using System;
using System.Collections.Generic;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Models;
using PlayoutEngine.OBS;
using System.Text.Json;

namespace PlayoutEngine.Services
{
    public interface IHealthCheckService
    {
        Task StartAsync(int port = 7001);
        Task StopAsync();
        void UpdateStatus(PlayoutState state, TimeSpan uptime, bool isObsConnected, bool isLicensed);
        void SetObsService(IObsIntegrationService obsService);
    }

    public class HealthCheckService : IHealthCheckService
    {
        private HttpListener? _listener;
        private bool _isRunning;
        private PlayoutState _currentState = PlayoutState.Stopped;
        private TimeSpan _uptime = TimeSpan.Zero;
        private bool _isObsConnected = false;
        private bool _isLicensed = false;
        private IObsIntegrationService? _obsService;
        private readonly ILogger<HealthCheckService> _logger;

        public HealthCheckService(ILogger<HealthCheckService> logger)
        {
            _logger = logger;
        }

        public void SetObsService(IObsIntegrationService obsService)
        {
            _obsService = obsService;
        }

        public async Task StartAsync(int port = 7001)
        {
            try
            {
                _listener = new HttpListener();
                _listener.Prefixes.Add($"http://localhost:{port}/");
                _listener.Start();

                _isRunning = true;
                _logger.LogInformation("HEALTH_CHECK_SERVER_STARTED port={Port}", port);

                // Start listening for requests
                _ = Task.Run(ListenAsync);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "HEALTH_CHECK_SERVER_START_ERROR port={Port} exception={ExceptionMessage}", 
                    port, ex.Message);
                throw;
            }
        }

        public async Task StopAsync()
        {
            _isRunning = false;
            _listener?.Stop();
            _listener?.Close();
            _logger.LogInformation("HEALTH_CHECK_SERVER_STOPPED");
        }

        public void UpdateStatus(PlayoutState state, TimeSpan uptime, bool isObsConnected, bool isLicensed)
        {
            _currentState = state;
            _uptime = uptime;
            _isObsConnected = isObsConnected;
            _isLicensed = isLicensed;
        }

        private async Task ListenAsync()
        {
            while (_isRunning)
            {
                try
                {
                    var context = await _listener.GetContextAsync();
                    await ProcessRequest(context);
                }
                catch (Exception ex)
                {
                    if (_isRunning) // Only log if we're supposed to be running
                    {
                        _logger.LogError(ex, "HEALTH_CHECK_REQUEST_ERROR exception={ExceptionMessage}", ex.Message);
                    }
                }
            }
        }

        private async Task ProcessRequest(HttpListenerContext context)
        {
            var request = context.Request;
            var response = context.Response;

            // Only handle the health check endpoint
            if (request.Url?.AbsolutePath == "/health")
            {
                await HandleHealthRequest(response);
            }
            else
            {
                // Return 404 for other endpoints
                response.StatusCode = 404;
                var responseString = "Not Found";
                var buffer = Encoding.UTF8.GetBytes(responseString);
                response.ContentLength64 = buffer.Length;
                await response.OutputStream.WriteAsync(buffer, 0, buffer.Length);
            }

            response.Close();
        }

        private async Task HandleHealthRequest(HttpListenerResponse response)
        {
            var obsState = _obsService?.ConnectionState.ToString() ?? "UNKNOWN";
            var isReconnecting = _obsService != null && _obsService.ConnectionState != ObsConnectionState.OBS_CONNECTED;

            var healthInfo = new
            {
                status = "running",
                uptime = (int)_uptime.TotalMilliseconds,
                currentState = _currentState.ToString(),
                obs = new
                {
                    state = obsState,
                    reconnecting = isReconnecting
                },
                isLicensed = _isLicensed,
                timestamp = DateTime.UtcNow,
                version = "1.0.0"
            };

            var jsonResponse = JsonSerializer.Serialize(healthInfo, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });

            response.StatusCode = 200;
            response.ContentType = "application/json";
            response.Headers.Add("Access-Control-Allow-Origin", "*");

            var buffer = Encoding.UTF8.GetBytes(jsonResponse);
            response.ContentLength64 = buffer.Length;
            await response.OutputStream.WriteAsync(buffer, 0, buffer.Length);

            _logger.LogDebug("HEALTH_CHECK_REQUEST_PROCESSED state={State} obs_state={ObsState} reconnecting={IsReconnecting} licensed={IsLicensed}",
                _currentState, obsState, isReconnecting, _isLicensed);
        }
    }
}