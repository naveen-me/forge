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
using Newtonsoft.Json.Linq;

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
        private readonly IBufferManager _bufferManager;
        private PlayoutState _currentState = PlayoutState.Stopped;
        private Timer? _stateTimer;
        private Timer? _healthTimer;
        private Timer? _syncTimer;  // For state reconciliation
        private Timer? _silenceWatchdogTimer; // For PHASE 3 - Silence Watchdog
        private FileSystemWatcher? _scheduleWatcher; // For PHASE 5 - JSON Hot-Reload
        private readonly PlayoutArbiter _arbiter;  // Added for patch 2
        private string _schedulePath = string.Empty;
        private Stopwatch _uptimeStopwatch = new Stopwatch();
        private TimelineConfigurationV2? _currentScheduleV2;
        private bool _isV2ScheduleLoaded = false; // Track if a V2 schedule is loaded
        private readonly Microsoft.Extensions.Configuration.IConfiguration _configuration;
        private DateTime _lastObsEventReceived = DateTime.MinValue; // For silence watchdog
        private readonly TimeSpan _silenceTimeout = TimeSpan.FromSeconds(10); // Configurable timeout
        private string _currentScheduleHash = string.Empty; // For hot-reload detection

        public PlayoutEngineService(
            ILogger<PlayoutEngineService> logger,
            ISchedulerService schedulerService,
            ITimelineSchedulerService timelineSchedulerService,
            IObsIntegrationService obsService,
            ILicenseService licenseService,
            IHealthCheckService healthCheckService,
            IBufferManager bufferManager,
            PlayoutArbiter arbiter,
            Microsoft.Extensions.Configuration.IConfiguration configuration)
        {
            _logger = logger;
            _schedulerService = schedulerService;
            _timelineSchedulerService = timelineSchedulerService;
            _obsService = obsService;
            _licenseService = licenseService;
            _healthCheckService = healthCheckService;
            _bufferManager = bufferManager;
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

            // Initialize buffer manager
            await _bufferManager.InitializeAsync();

            // Initialize OBS connection
            _logger.LogInformation("CONNECTING_OBS host={Host} port={Port}", obsHost, obsPort);
            var obsConnected = await _obsService.ConnectToObsAsync(obsHost, obsPort, obsPassword);
            if (obsConnected)
            {
                _logger.LogInformation("OBS_CONNECTED");

                // Subscribe to OBS events for PHASE 2 - OBS Script Event-Driven
                _obsService.OnMediaEvent += OnObsMediaEvent;
                _obsService.OnSceneOrVisibilityEvent += OnObsSceneOrVisibilityEvent;
                _obsService.OnObsConnected += OnObsConnected;
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

            // Start silence watchdog timer (PHASE 3)
            _silenceWatchdogTimer = new Timer(CheckSilenceWatchdog, null, TimeSpan.FromSeconds(5), TimeSpan.FromSeconds(5));
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

                        // Set up file watcher for hot-reload (PHASE 5)
                        SetupScheduleWatcher(schedulePath);
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

        private void SetupScheduleWatcher(string schedulePath)
        {
            try
            {
                var directory = Path.GetDirectoryName(schedulePath);
                var fileName = Path.GetFileName(schedulePath);

                if (string.IsNullOrEmpty(directory) || string.IsNullOrEmpty(fileName))
                    return;

                _scheduleWatcher = new FileSystemWatcher(directory, fileName);
                _scheduleWatcher.NotifyFilter = NotifyFilters.LastWrite | NotifyFilters.CreationTime | NotifyFilters.Size;
                _scheduleWatcher.Changed += OnScheduleFileChanged;
                _scheduleWatcher.EnableRaisingEvents = true;

                // Calculate initial hash
                _currentScheduleHash = CalculateFileHash(schedulePath);

                _logger.LogInformation("SCHEDULE_WATCHER_SETUP watching: {SchedulePath}", schedulePath);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "ERROR_SETTING_UP_SCHEDULE_WATCHER for path: {SchedulePath}", schedulePath);
            }
        }

        private string CalculateFileHash(string filePath)
        {
            if (!File.Exists(filePath))
                return string.Empty;

            using var sha256 = System.Security.Cryptography.SHA256.Create();
            using var stream = File.OpenRead(filePath);
            var hashBytes = sha256.ComputeHash(stream);
            return Convert.ToBase64String(hashBytes);
        }

        private async void OnScheduleFileChanged(object source, FileSystemEventArgs e)
        {
            _logger.LogInformation("SCHEDULE_FILE_CHANGED detected: {FilePath}", e.FullPath);

            // Debounce multiple rapid changes
            await Task.Delay(500);

            try
            {
                var newHash = CalculateFileHash(e.FullPath);
                if (newHash != _currentScheduleHash)
                {
                    _logger.LogInformation("SCHEDULE_CONTENT_CHANGED - triggering reload");

                    // Update the hash
                    _currentScheduleHash = newHash;

                    // Reload the schedule
                    var loaded = await LoadSchedule(e.FullPath);
                    if (loaded)
                    {
                        _logger.LogInformation("SCHEDULE_RELOADED_SUCCESSFULLY");

                        // Trigger immediate reconciliation to reflect changes
                        await ReconcileStateAsync();
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "ERROR_PROCESSING_SCHEDULE_CHANGE for file: {FilePath}", e.FullPath);
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

        private void OnObsMediaEvent(string eventType, JObject data)
        {
            _logger.LogDebug("OBS Media Event: {EventType}", eventType);
            _lastObsEventReceived = DateTime.UtcNow; // Update last event received time

            // Handle media completion events that should advance the buffer
            if (eventType == "MediaEnded" || eventType == "MediaStopped")
            {
                var sourceName = data["sourceName"]?.ToString();
                _logger.LogInformation("Media completed: {SourceName}", sourceName);

                // Trigger immediate reconciliation to move to next item
                _ = Task.Run(async () =>
                {
                    try
                    {
                        await ReconcileStateAsync();
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Error during media completion reconciliation");
                    }
                });
            }
        }

        private void OnObsSceneOrVisibilityEvent(string eventType, JObject data)
        {
            _logger.LogDebug("OBS Scene/Visibility Event: {EventType}", eventType);
            _lastObsEventReceived = DateTime.UtcNow; // Update last event received time

            // For scene changes, immediately reconcile to restore correct state
            if (eventType == "SceneChanged")
            {
                _logger.LogInformation("OBS Scene changed - triggering immediate reconciliation");

                // Trigger immediate reconciliation to restore correct state
                _ = Task.Run(async () =>
                {
                    try
                    {
                        await ReconcileStateAsync();
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Error during scene change reconciliation");
                    }
                });
            }
        }

        private void OnObsConnected()
        {
            _logger.LogInformation("OBS_RECONNECTED - rebuilding state");

            // After OBS reconnects, rebuild the entire state
            _ = Task.Run(async () =>
            {
                try
                {
                    // Wait a bit for OBS to fully initialize
                    await Task.Delay(2000);

                    await ReconcileStateAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error during OBS reconnection state rebuild");
                }
            });
        }

        private void CheckSilenceWatchdog(object? state)
        {
            // Check if we've received any OBS events recently
            var timeSinceLastEvent = DateTime.UtcNow - _lastObsEventReceived;

            if (timeSinceLastEvent > _silenceTimeout)
            {
                _logger.LogWarning("SILENCE_WATCHDOG_TRIGGERED - No OBS events for {Duration} seconds, performing verification", timeSinceLastEvent.TotalSeconds);

                // Perform immediate reconciliation
                _ = Task.Run(async () =>
                {
                    try
                    {
                        await ReconcileStateAsync();
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Error during silence watchdog reconciliation");
                    }
                });
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
                    if (loaded)
                    {
                        // Mark that a V2 schedule is loaded
                        _isV2ScheduleLoaded = true;
                        // Also load it into the timeline scheduler service
                        await _timelineSchedulerService.LoadTimelineScheduleAsync(schedulePath);
                    }
                }
                else
                {
                    _logger.LogInformation("LOADING_V1_SCHEMA schedule_path={SchedulePath}", schedulePath);
                    loaded = await _schedulerService.LoadScheduleAsync(schedulePath);
                    if (loaded)
                    {
                        // Mark that a V1 schedule is loaded
                        _isV2ScheduleLoaded = false;
                    }
                }

                if (loaded)
                {
                    _schedulePath = schedulePath;
                    TransitionToState(PlayoutState.Loading);
                    _logger.LogInformation("SCHEDULE_LOADED_SUCCESS path={SchedulePath} schema_version={SchemaVersion}",
                        schedulePath, schemaVersion);

                    // Set up file watcher for hot-reload (PHASE 5)
                    SetupScheduleWatcher(schedulePath);

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
                if (_isV2ScheduleLoaded)
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
                if (_isV2ScheduleLoaded)
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
                if (_isV2ScheduleLoaded)
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

        public string GetCurrentSchedulePath()
        {
            return _schedulePath;
        }

        public int GetBufferItemCount()
        {
            // Since we don't have direct access to the buffer manager's count property,
            // we'll need to inject it or create a method to access it
            // For now, we'll return a placeholder - this would need to be implemented properly
            return _bufferManager.BufferItemCount;
        }

        public PlayoutDecision? GetCurrentDecision(DateTime now)
        {
            // Use the arbiter to get the current decision
            return _arbiter.PeekDecision(now);
        }

        private SyncAction DecideSync(ExpectedState expected, ActualObsState? actual)
        {
            if (actual == null)
                return SyncAction.HARD_SYNC;

            // If no actual media is playing but we expect something, or if media IDs don't match
            if (string.IsNullOrEmpty(actual.MediaId) || actual.MediaId != expected.MediaId)
                return SyncAction.HARD_SYNC;

            // Check if media is playing but we expect logo, or vice versa
            if ((expected.MediaId == "GLOBAL_LOGO") != (actual.MediaId == "GLOBAL_LOGO"))
                return SyncAction.HARD_SYNC;

            // For media content, check if offset is significantly different
            if (expected.MediaId != "GLOBAL_LOGO" && Math.Abs(actual.Offset - expected.Offset) > 0.5)
                return SyncAction.SOFT_SYNC;

            // Check if expected media should be visible but isn't
            if (actual.MediaId == expected.MediaId && !actual.VisibleSources.Contains(expected.MediaId))
                return SyncAction.HARD_SYNC;

            return SyncAction.NOOP;
        }

        private async Task ReconcileStateAsync()
        {
            try
            {
                // Get the expected state from the arbiter
                var decision = _arbiter.PeekDecision(DateTime.Now); // Use Peek to not advance buffer
                if (decision == null) return;

                var expected = new ExpectedState
                {
                    MediaId = decision.MediaId ?? "fallback",
                    Offset = decision.Offset,
                    Path = decision.Content?.ToString() ?? ""
                };

                // Get actual OBS state
                var actual = await GetActualObsStateAsync();

                // Decide what sync action to take
                switch (DecideSync(expected, actual))
                {
                    case SyncAction.NOOP:
                        return; // No action needed

                    case SyncAction.SOFT_SYNC:
                        _obsService.SetMediaTime(expected.MediaId, expected.Offset);
                        _logger.LogDebug("SOFT_SYNC applied - set media time for {MediaId} to {Offset}", expected.MediaId, expected.Offset);
                        return;

                    case SyncAction.HARD_SYNC:
                        // Apply expected state to OBS
                        await ApplyExpectedStateAsync(expected);
                        _logger.LogDebug("HARD_SYNC applied - rebuilt OBS state for {MediaId}", expected.MediaId);
                        return;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "RECONCILIATION_ERROR");
            }
        }

        private async void ReconcileState(object? state)
        {
            _ = ReconcileStateAsync(); // Fire and forget for timer-based reconciliation
        }

        private async Task<ActualObsState?> GetActualObsStateAsync()
        {
            if (_obsService.ConnectionState != ObsConnectionState.OBS_CONNECTED)
            {
                return null;
            }

            try
            {
                // Get current scene
                var currentSceneResponse = await _obsService.GetCurrentSceneAsync();
                if (currentSceneResponse?.CurrentProgramSceneName == null)
                {
                    return null;
                }

                var actualState = new ActualObsState
                {
                    SceneName = currentSceneResponse.CurrentProgramSceneName,
                    VisibleSources = new List<string>()
                };

                // Get scene items to determine which sources are visible
                var sceneItemsResponse = await _obsService.GetSceneItemsAsync(currentSceneResponse.CurrentProgramSceneName);
                if (sceneItemsResponse?.SceneItems != null)
                {
                    foreach (var item in sceneItemsResponse.SceneItems)
                    {
                        if (item.SceneItemEnabled && !string.IsNullOrEmpty(item.SourceName))
                        {
                            actualState.VisibleSources.Add(item.SourceName);

                            // If this is a media source, get its status
                            if (item.InputKind == "ffmpeg_source") // Media source
                            {
                                var mediaStatus = await _obsService.GetMediaInputStatusAsync(item.SourceName);
                                if (mediaStatus != null)
                                {
                                    actualState.MediaId = item.SourceName;
                                    actualState.Offset = mediaStatus.MediaCursor ?? 0.0;
                                }
                            }
                        }
                    }
                }

                return actualState;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting actual OBS state");
                return null;
            }
        }

        private async Task ApplyExpectedStateAsync(ExpectedState expected)
        {
            if (_obsService.ConnectionState != ObsConnectionState.OBS_CONNECTED)
                return;

            // First, hide all current media sources
            var currentSceneResponse = await _obsService.GetCurrentSceneAsync();
            if (currentSceneResponse?.CurrentProgramSceneName != null)
            {
                var sceneItems = await _obsService.GetSceneItemsAsync(currentSceneResponse.CurrentProgramSceneName);
                if (sceneItems?.SceneItems != null)
                {
                    foreach (var item in sceneItems.SceneItems)
                    {
                        if (item.SceneItemEnabled && !string.IsNullOrEmpty(item.SourceName))
                        {
                            // Hide all sources except for the global logo which we might want to keep
                            if (item.SourceName != "GLOBAL_LOGO")
                            {
                                _obsService.SetSourceVisibility(item.SourceName, false);
                            }
                        }
                    }
                }
            }

            // Handle different types of content
            if (expected.MediaId == "GLOBAL_LOGO")
            {
                // Handle global logo - create image source for logo
                var logoPath = _configuration.GetValue<string>("Logo:Path") ?? "media-assets/logo.png";

                // Check if logo source exists
                var logoSettings = await _obsService.GetInputSettingsAsync("GLOBAL_LOGO");
                if (logoSettings == null)
                {
                    // Create logo source
                    var settings = new
                    {
                        file = logoPath
                    };

                    _obsService.CreateSource("GLOBAL_LOGO", "image_source", "Scene", settings);
                }
                else
                {
                    // Update logo path if needed
                    if (logoSettings.InputSettings != null &&
                        logoSettings.InputSettings.ContainsKey("file") &&
                        logoSettings.InputSettings["file"]?.ToString() != logoPath)
                    {
                        var settings = new
                        {
                            file = logoPath
                        };

                        _obsService.SetSourceProperties("GLOBAL_LOGO", settings);
                    }
                }

                // Show the logo
                _obsService.SetSourceVisibility("GLOBAL_LOGO", true);
            }
            else
            {
                // Handle media content
                var inputSettings = await _obsService.GetInputSettingsAsync(expected.MediaId);
                if (inputSettings == null)
                {
                    // Source doesn't exist, create it
                    var settings = new
                    {
                        is_local_file = true,
                        local_file = expected.Path,
                        looping = false
                    };

                    _obsService.CreateSource(expected.MediaId, "ffmpeg_source", "Scene", settings);
                }
                else
                {
                    // Source exists, update its path if needed
                    if (inputSettings.InputSettings != null &&
                        inputSettings.InputSettings.ContainsKey("local_file") &&
                        inputSettings.InputSettings["local_file"]?.ToString() != expected.Path)
                    {
                        var settings = new
                        {
                            is_local_file = true,
                            local_file = expected.Path,
                            looping = false
                        };

                        _obsService.SetSourceProperties(expected.MediaId, settings);
                    }
                }

                // Show the media source
                _obsService.SetSourceVisibility(expected.MediaId, true);

                // Play the media and set the time
                _obsService.PlayMedia(expected.MediaId, expected.Path);
                _obsService.SetMediaTime(expected.MediaId, expected.Offset);
            }

            // Apply overlays if any
            if (expected.Overlays != null)
            {
                foreach (var overlay in expected.Overlays)
                {
                    if (!string.IsNullOrEmpty(overlay.Source))
                    {
                        _obsService.SetSourceVisibility(overlay.Source, true);
                    }
                }
            }
        }

        private void ApplyExpectedState(ExpectedState expected)
        {
            _ = Task.Run(async () => await ApplyExpectedStateAsync(expected));
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

            // Unsubscribe from OBS events
            _obsService.OnMediaEvent -= OnObsMediaEvent;
            _obsService.OnSceneOrVisibilityEvent -= OnObsSceneOrVisibilityEvent;
            _obsService.OnObsConnected -= OnObsConnected;

            // Stop file watcher
            if (_scheduleWatcher != null)
            {
                _scheduleWatcher.EnableRaisingEvents = false;
                _scheduleWatcher.Changed -= OnScheduleFileChanged;
                _scheduleWatcher.Dispose();
            }

            // Disconnect from OBS
            _logger.LogInformation("DISCONNECTING_OBS");
            await _obsService.DisconnectAsync();

            // Stop timers
            _stateTimer?.Dispose();
            _healthTimer?.Dispose();
            _syncTimer?.Dispose();
            _silenceWatchdogTimer?.Dispose();
            _uptimeStopwatch.Stop();

            // Stop health check service
            await _healthCheckService.StopAsync();

            _logger.LogInformation("PLAYOUT_ENGINE_SERVICE_STOPPED");
            await base.StopAsync(cancellationToken);
        }
    }
}