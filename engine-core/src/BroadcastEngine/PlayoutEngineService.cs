using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Services;
using PlayoutEngine.OBS;
using PlayoutEngine.Licensing;
using PlayoutEngine.Models;
using System.IO;
using System.Diagnostics;
using System.Text.Json;
using Microsoft.Extensions.Configuration;

namespace PlayoutEngine
{
    public class PlayoutEngineService : BackgroundService
    {
        // Enums for reconciliation logic
        enum SyncAction
        {
            NOOP,
            SOFT_SYNC,
            HARD_SYNC
        }

        private readonly ILogger<PlayoutEngineService> _logger;
        private readonly ISchedulerService _schedulerService;
        private readonly ITimelineSchedulerService _timelineSchedulerService;
        private readonly IObsIntegrationService _obsService;
        private readonly ILicenseService _licenseService;
        private readonly IHealthCheckService _healthCheckService;
        private PlayoutState _currentState = PlayoutState.Stopped;
        private Timer? _stateTimer;
        private Timer? _healthTimer;
        private Timer? _syncTimer;  // For state reconciliation
        private readonly PlayoutArbiter _arbiter;  // Added for patch 2
        private string _schedulePath = string.Empty;
        private Stopwatch _uptimeStopwatch = new Stopwatch();
        private TimelineConfigurationV2? _currentScheduleV2;
        private readonly Microsoft.Extensions.Configuration.IConfiguration _configuration;

        public PlayoutEngineService(
            ILogger<PlayoutEngineService> logger,
            ISchedulerService schedulerService,
            ITimelineSchedulerService timelineSchedulerService,
            IObsIntegrationService obsService,
            ILicenseService licenseService,
            IHealthCheckService healthCheckService,
            PlayoutArbiter arbiter,
            Microsoft.Extensions.Configuration.IConfiguration configuration)
        {
            _logger = logger;
            _schedulerService = schedulerService;
            _timelineSchedulerService = timelineSchedulerService;
            _obsService = obsService;
            _licenseService = licenseService;
            _healthCheckService = healthCheckService;
            _arbiter = arbiter;
            _configuration = configuration;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("PLAYOUT_ENGINE_STARTING");

            // Initialize services
            await InitializeServices();

            // Set the OBS service for health check
            _healthCheckService.SetObsService(_obsService);

            // Start the health check service
            await _healthCheckService.StartAsync(7001); // Default port - in real implementation, load from config

            // Start the uptime stopwatch
            _uptimeStopwatch.Start();

            // Start state monitoring
            _stateTimer = new Timer(UpdateState, null, TimeSpan.Zero, TimeSpan.FromSeconds(1));

            // Start health monitoring
            _healthTimer = new Timer(UpdateHealthStatus, null, TimeSpan.Zero, TimeSpan.FromSeconds(5));

            // Start state reconciliation loop (sync with OBS)
            _syncTimer = new Timer(ReconcileState, null, TimeSpan.Zero, TimeSpan.FromMilliseconds(100));

            while (!stoppingToken.IsCancellationRequested)
            {
                // Main service loop
                await Task.Delay(100, stoppingToken);
            }

            _logger.LogInformation("PLAYOUT_ENGINE_STOPPING");
        }

        private async Task InitializeServices()
        {
            _logger.LogInformation("INITIALIZING_SERVICES");

            var obsHost = _configuration.GetValue<string>("Obs:Host") ?? "localhost";
            var obsPort = _configuration.GetValue<int>("Obs:Port");
            var obsPassword = _configuration.GetValue<string>("Obs:Password") ?? string.Empty;


            // Initialize OBS connection
            _logger.LogInformation("CONNECTING_OBS host={Host} port={Port}", obsHost, obsPort);
            var obsConnected = await _obsService.ConnectToObsAsync(obsHost, obsPort, obsPassword);
            if (obsConnected)
            {
                _logger.LogInformation("OBS_CONNECTED");
            }
            else
            {
                _logger.LogError("OBS_CONNECTION_FAILED");
                // Start automatic reconnection to handle OBS starting later
                _obsService.StartAutoReconnect();
                _logger.LogInformation("OBS_AUTO_RECONNECT_STARTED");
            }

            // Check license validity
            _logger.LogInformation("CHECKING_LICENSE");
            var isLicensed = await _licenseService.IsLicenseValidAsync();
            if (isLicensed)
            {
                _logger.LogInformation("LICENSE_VALID");
            }
            else
            {
                _logger.LogError("LICENSE_INVALID playback_operations_restricted");
            }

            // Set initial state
            _currentState = PlayoutState.Stopped;
            _logger.LogInformation("INITIALIZATION_COMPLETE state={State}", _currentState);

            // Try to load default schedule automatically
            await LoadDefaultSchedule();
        }

        private async Task LoadDefaultSchedule()
        {
            try
            {
                // Look for default schedule file in the schedules directory at project root
                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var scheduleFiles = Directory.GetFiles(schedulesDir, "*.json", SearchOption.TopDirectoryOnly);

                if (scheduleFiles.Length > 0)
                {
                    // Use the first schedule file found (or prioritize by name)
                    var schedulePath = scheduleFiles[0];
                    foreach (var file in scheduleFiles)
                    {
                        if (file.Contains("today") || file.Contains("default"))
                        {
                            schedulePath = file;
                            break;
                        }
                    }

                    _logger.LogInformation("AUTO_LOADING_SCHEDULE file={ScheduleFile}", Path.GetFileName(schedulePath));

                    bool loaded = await LoadSchedule(schedulePath);
                    if (loaded)
                    {
                        _logger.LogInformation("AUTO_SCHEDULE_LOADED_SUCCESS starting playback...");

                        // Start the schedule playback
                        await StartPlayout();
                    }
                    else
                    {
                        _logger.LogWarning("AUTO_SCHEDULE_LOAD_FAILED continuing without schedule");
                    }
                }
                else
                {
                    _logger.LogInformation("NO_SCHEDULE_FILES_FOUND in schedules directory");
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "AUTO_SCHEDULE_LOAD_ERROR exception={ExceptionMessage}", ex.Message);
                // Still continue without schedule, allowing buffer/logo to work
            }
        }

        private void UpdateState(object? state)
        {
            // This method runs every second to update the state machine
            try
            {
                var licenseValid = _licenseService.IsLicenseValidAsync().Result;

                if (!licenseValid && _currentState != PlayoutState.Stopped)
                {
                    // License expired, stop operations
                    _logger.LogWarning("LICENSE_EXPIRED state_transition_from={State}", _currentState);
                    TransitionToState(PlayoutState.Error);
                }

                // Add any state-specific logic here
                switch (_currentState)
                {
                    case PlayoutState.Error:
                        _logger.LogWarning("ENGINE_ERROR_CHECK_LICENSE_AND_OBS_CONNECTION");
                        break;
                    case PlayoutState.Playing:
                        // Monitor ongoing playout
                        _logger.LogDebug("PLAYOUT_STATE_MONITORING active_state={State}", _currentState);
                        break;
                    case PlayoutState.Stopped:
                        // Waiting for schedule load command
                        _logger.LogDebug("ENGINE_WAITING_FOR_SCHEDULE state={State}", _currentState);
                        break;
                    default:
                        // Other states handled as needed
                        _logger.LogDebug("ENGINE_STATE_MONITORING state={State}", _currentState);
                        break;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "STATE_UPDATE_ERROR exception={ExceptionMessage}", ex.Message);
            }
        }

        private void UpdateHealthStatus(object? state)
        {
            // This method runs every 5 seconds to update health status
            try
            {
                var uptime = _uptimeStopwatch.Elapsed;
                var isObsConnected = _obsService.ConnectionState == ObsConnectionState.OBS_CONNECTED;
                var isLicensed = _licenseService.IsLicenseValidAsync().Result;

                _healthCheckService.UpdateStatus(_currentState, uptime, isObsConnected, isLicensed);

                _logger.LogDebug("HEALTH_STATUS_UPDATED state={State} uptime={Uptime} obs_connected={IsObsConnected} licensed={IsLicensed}",
                    _currentState, (int)uptime.TotalMilliseconds, isObsConnected, isLicensed);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "HEALTH_STATUS_UPDATE_ERROR exception={ExceptionMessage}", ex.Message);
            }
        }

        public async Task<bool> LoadSchedule(string schedulePath)
        {
            _logger.LogInformation("SCHEDULE_LOAD_REQUESTED path={SchedulePath} current_state={CurrentState}",
                schedulePath, _currentState);

            if (_currentState != PlayoutState.Stopped && _currentState != PlayoutState.Error)
            {
                _logger.LogWarning("SCHEDULE_LOAD_BLOCKED_INVALID_STATE current_state={CurrentState}", _currentState);
                return false;
            }

            try
            {
                // Verify license before loading schedule
                _logger.LogInformation("VERIFYING_LICENSE_FOR_SCHEDULE_LOAD");
                var isLicensed = await _licenseService.IsLicenseValidAsync();
                if (!isLicensed)
                {
                    _logger.LogError("SCHEDULE_LOAD_LICENSE_CHECK_FAILED");
                    TransitionToState(PlayoutState.Error);
                    return false;
                }

                // Determine schema version by reading the JSON temporarily
                var jsonContent = await System.IO.File.ReadAllTextAsync(schedulePath);
                var schemaVersion = "1.0"; // default to V1

                try
                {
                    using var jsonDoc = System.Text.Json.JsonDocument.Parse(jsonContent);
                    JsonElement versionElement;

                    // Try the expected property name first
                    if (jsonDoc.RootElement.TryGetProperty("schemaVersion", out versionElement))
                    {
                        schemaVersion = versionElement.GetString() ?? "1.0";
                    }
                    // If that fails, try alternative casing
                    else if (jsonDoc.RootElement.TryGetProperty("SchemaVersion", out versionElement))
                    {
                        schemaVersion = versionElement.GetString() ?? "1.0";
                    }
                }
                catch
                {
                    // If parsing fails or schemaVersion is not found, assume V1 format
                    schemaVersion = "1.0";
                }

                bool loaded = false;
                if (schemaVersion == "2.0")
                {
                    _logger.LogInformation("LOADING_V2_SCHEMA schedule_path={SchedulePath}", schedulePath);
                    loaded = await _schedulerService.LoadScheduleV2Async(schedulePath);
                }
                else
                {
                    _logger.LogInformation("LOADING_V1_SCHEMA schedule_path={SchedulePath}", schedulePath);
                    loaded = await _schedulerService.LoadScheduleAsync(schedulePath);
                }

                if (loaded)
                {
                    _schedulePath = schedulePath;
                    TransitionToState(PlayoutState.Loading);
                    _logger.LogInformation("SCHEDULE_LOADED_SUCCESS path={SchedulePath} schema_version={SchemaVersion}",
                        schedulePath, schemaVersion);
                    return true;
                }
                else
                {
                    _logger.LogWarning("SCHEDULE_LOAD_FAILED path={SchedulePath}", schedulePath);
                    // Don't transition to error state for failed schedule - use fallback instead
                    _logger.LogWarning("SCHEDULE_INVALID_RUNNING_FALLBACK");
                    // Keep running in Stopped state to allow buffer/logo to play
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "SCHEDULE_LOAD_ERROR path={SchedulePath} exception={ExceptionMessage}",
                    schedulePath, ex.Message);
                // Don't transition to error state for failed schedule - use fallback instead
                _logger.LogWarning("SCHEDULE_INVALID_RUNNING_FALLBACK");
                // Keep running in Stopped state to allow buffer/logo to play
                return false;
            }
        }

        public async Task<bool> StartPlayout()
        {
            _logger.LogInformation("PLAYOUT_START_REQUESTED current_state={CurrentState}", _currentState);

            if (_currentState != PlayoutState.Loading && _currentState != PlayoutState.Paused)
            {
                _logger.LogWarning("PLAYOUT_START_BLOCKED_INVALID_STATE current_state={CurrentState}", _currentState);
                return false;
            }

            try
            {
                var isLicensed = await _licenseService.IsLicenseValidAsync();
                if (!isLicensed)
                {
                    _logger.LogError("PLAYOUT_START_LICENSE_CHECK_FAILED");
                    TransitionToState(PlayoutState.Error);
                    return false;
                }

                // Determine which scheduler to use based on the loaded schedule type
                if (_currentScheduleV2 != null)
                {
                    _logger.LogInformation("STARTING_V2_TIMELINE_SCHEDULE");
                    await _timelineSchedulerService.StartScheduleAsync();
                }
                else
                {
                    _logger.LogInformation("STARTING_V1_SCHEDULE");
                    await _schedulerService.StartScheduleAsync();
                }

                TransitionToState(PlayoutState.Playing);
                _logger.LogInformation("PLAYOUT_STARTED");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "PLAYOUT_START_ERROR exception={ExceptionMessage}", ex.Message);
                TransitionToState(PlayoutState.Error);
                return false;
            }
        }

        public async Task<bool> PausePlayout()
        {
            _logger.LogInformation("PLAYOUT_PAUSE_REQUESTED current_state={CurrentState}", _currentState);

            if (_currentState != PlayoutState.Playing)
            {
                _logger.LogWarning("PLAYOUT_PAUSE_BLOCKED_INVALID_STATE current_state={CurrentState}", _currentState);
                return false;
            }

            try
            {
                // Determine which scheduler to pause based on the loaded schedule type
                if (_currentScheduleV2 != null)
                {
                    await _timelineSchedulerService.PauseScheduleAsync();
                }
                else
                {
                    await _schedulerService.PauseScheduleAsync();
                }

                TransitionToState(PlayoutState.Paused);
                _logger.LogInformation("PLAYOUT_PAUSED");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "PLAYOUT_PAUSE_ERROR exception={ExceptionMessage}", ex.Message);
                TransitionToState(PlayoutState.Error);
                return false;
            }
        }

        public async Task<bool> StopPlayout()
        {
            _logger.LogInformation("PLAYOUT_STOP_REQUESTED current_state={CurrentState}", _currentState);

            if (_currentState == PlayoutState.Stopped)
            {
                _logger.LogWarning("PLAYOUT_ALREADY_STOPPED");
                return true;
            }

            try
            {
                // Determine which scheduler to stop based on the loaded schedule type
                if (_currentScheduleV2 != null)
                {
                    await _timelineSchedulerService.StopScheduleAsync();
                }
                else
                {
                    await _schedulerService.StopScheduleAsync();
                }

                TransitionToState(PlayoutState.Stopped);
                _logger.LogInformation("PLAYOUT_STOPPED");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "PLAYOUT_STOP_ERROR exception={ExceptionMessage}", ex.Message);
                TransitionToState(PlayoutState.Error);
                return false;
            }
        }

        private void TransitionToState(PlayoutState newState)
        {
            if (_currentState == newState) return;

            var oldState = _currentState;
            _currentState = newState;
            _logger.LogInformation("STATE_TRANSITION from={OldState} to={NewState}", oldState, newState);
        }

        public PlayoutState GetCurrentState()
        {
            return _currentState;
        }

        private SyncAction DecideSync(ExpectedState expected, ActualObsState? actual)
        {
            if (actual == null)
                return SyncAction.HARD_SYNC;

            if (actual.MediaId != expected.MediaId)
                return SyncAction.HARD_SYNC;

            if (Math.Abs(actual.Offset - expected.Offset) > 0.5)
                return SyncAction.SOFT_SYNC;

            return SyncAction.NOOP;
        }

        private void ReconcileState(object? state)
        {
            try
            {
                // Get the expected state from the arbiter
                var decision = _arbiter.Decide(DateTime.Now);
                if (decision == null) return;

                var expected = new ExpectedState
                {
                    MediaId = decision.MediaId ?? "fallback",
                    Offset = decision.Offset,
                    Path = decision.Content?.ToString() ?? ""
                };

                // Get actual OBS state (this is a simplified version - in reality you would query OBS)
                var actual = GetActualObsState();

                // Decide what sync action to take
                switch (DecideSync(expected, actual))
                {
                    case SyncAction.NOOP:
                        return; // No action needed

                    case SyncAction.SOFT_SYNC:
                        _obsService.SetMediaTime(expected.MediaId, expected.Offset);
                        return;

                    case SyncAction.HARD_SYNC:
                        // Apply expected state to OBS
                        ApplyExpectedState(expected);
                        return;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "RECONCILIATION_ERROR");
            }
        }

        private ActualObsState? GetActualObsState()
        {
            // This would query the actual OBS state in a real implementation
            // For now, we return a placeholder
            return new ActualObsState
            {
                MediaId = "current_media",
                Offset = 0.0,
                SceneName = "Scene 1"
            };
        }

        private void ApplyExpectedState(ExpectedState expected)
        {
            // This would apply the expected state to OBS in a real implementation
            if (_obsService.ConnectionState == ObsConnectionState.OBS_CONNECTED)
            {
                // Example implementation - would need to be adapted based on actual media type
                _obsService.PlayMedia(expected.MediaId, expected.Path);
                _obsService.SetMediaTime(expected.MediaId, expected.Offset);
            }
        }

        public override async Task StopAsync(CancellationToken cancellationToken)
        {
            _logger.LogInformation("PLAYOUT_ENGINE_SERVICE_STOP_REQUESTED");

            // Stop active playout if running
            if (_currentState == PlayoutState.Playing || _currentState == PlayoutState.Paused)
            {
                _logger.LogInformation("STOPPING_ACTIVE_PLAYOUT");
                await StopPlayout();
            }

            // Disconnect from OBS
            _logger.LogInformation("DISCONNECTING_OBS");
            await _obsService.DisconnectAsync();

            // Stop timers
            _stateTimer?.Dispose();
            _healthTimer?.Dispose();
            _syncTimer?.Dispose();
            _uptimeStopwatch.Stop();

            // Stop health check service
            await _healthCheckService.StopAsync();

            _logger.LogInformation("PLAYOUT_ENGINE_SERVICE_STOPPED");
            await base.StopAsync(cancellationToken);
        }
    }
}