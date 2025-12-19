using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Timers;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Models;
using Websocket.Client;
using Newtonsoft.Json.Linq;
using System.Reactive.Linq;
using System.Security.Cryptography;
using System.Text;

namespace PlayoutEngine.OBS
{
    public enum ObsConnectionState
    {
        OBS_DISCONNECTED,
        OBS_CONNECTING,
        OBS_CONNECTED,
        OBS_ERROR
    }

    public interface IObsIntegrationService
    {
        Task<bool> ConnectToObsAsync(string host, int port, string password);
        bool SetScene(string sceneName);
        bool SetSourceVisibility(string sourceName, bool visible);
        bool PlayMedia(string mediaSourceName, string filePath);
        bool StopMedia(string mediaSourceName);
        bool SetSourceProperties(string sourceName, object properties);
        bool SetSourceTransform(string sourceName, Transform transform);
        bool SetSourceCrop(string sourceName, int top, int bottom, int left, int right);
        bool CreateSource(string sourceName, string sourceType, string sceneName, object settings);
        bool DeleteSource(string sourceName, string sceneName);
        bool SetSourceFilterVisibility(string sourceName, string filterName, bool visible);
        bool SetSourceFilterSettings(string sourceName, string filterName, object settings);
        bool SetMediaTime(string mediaSourceName, double timeSeconds);
        bool GetMediaTime(string mediaSourceName);
        Task DisconnectAsync();
        void StartAutoReconnect();
        void StopAutoReconnect();
        ObsConnectionState ConnectionState { get; }
    }

    public class ObsIntegrationService : IObsIntegrationService
    {
        private WebsocketClient? _websocketClient;
        private bool _isConnected;
        private readonly object _lockObject = new object();
        private readonly Dictionary<string, string> _requestIdMap = new Dictionary<string, string>();
        private readonly Microsoft.Extensions.Logging.ILogger<ObsIntegrationService> _logger;

        // Fields for reconnection mechanism
        private string _host = string.Empty;
        private int _port = 0;
        private string _password = string.Empty;
        private bool _shouldReconnect = false;
        private System.Threading.Timer? _reconnectTimer;
        private readonly TimeSpan _reconnectInterval = TimeSpan.FromSeconds(5);
        private DateTime _lastReconnectAttempt = DateTime.MinValue;

        // Fields for new reconnect loop


        // Fields for new reconnect loop
        private CancellationTokenSource? _reconnectCts;
        private Task? _reconnectTask;

        // OBS connection state
        private ObsConnectionState _connectionState = ObsConnectionState.OBS_DISCONNECTED;
        private string _authChallenge = string.Empty;
        private string _authSalt = string.Empty;
        private bool _isObsV5 = false;

        public ObsConnectionState ConnectionState => _connectionState;

        public ObsIntegrationService(Microsoft.Extensions.Logging.ILogger<ObsIntegrationService> logger)
        {
            _logger = logger;
        }
        private void OnConnection(object info)
        {
            _logger.LogInformation("Connected to OBS");

            _isConnected = true;
            _connectionState = ObsConnectionState.OBS_CONNECTED;
            StopReconnectLoop();
            _logger.LogInformation("OBS_CONNECTED");
            _lastReconnectAttempt = DateTime.UtcNow;
        }

        public async Task<bool> ConnectToObsAsync(string host, int port, string password)
        {
            try
            {
                _host = host;
                _port = port;
                _password = password;

                var url = new Uri($"ws://{host}:{port}");

                // Dispose existing client if any
                if (_websocketClient != null)
                {
                    await _websocketClient.Stop(System.Net.WebSockets.WebSocketCloseStatus.NormalClosure, "Reconnecting with new parameters");
                    _websocketClient.Dispose();
                }

                _websocketClient = new WebsocketClient(url);

                // Disable automatic reconnect since we handle it ourselves
                _websocketClient.ReconnectTimeout = null;

                // Set up event handlers
                _websocketClient.MessageReceived.Subscribe(HandleMessage);
                _websocketClient.DisconnectionHappened.Subscribe(info => OnDisconnection(info));
                _websocketClient.ReconnectionHappened.Subscribe(info => OnConnection(info));

                _connectionState = ObsConnectionState.OBS_CONNECTING;

                await _websocketClient.Start();

                // Wait briefly to allow connection to establish
                await Task.Delay(1000);

                return _isConnected;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error connecting to OBS: {Message}", ex.Message);
                _connectionState = ObsConnectionState.OBS_ERROR;
                return false;
            }
        }

        private void HandleMessage(ResponseMessage msg)
        {
            try
            {
                var jsonObject = JObject.Parse(msg.Text);

                if (jsonObject.ContainsKey("op"))
                {
                    var opToken = jsonObject["op"];
                    var opCode = opToken != null ? (int)opToken : -1; // Provide a default value

                    var data = jsonObject["d"];

                    if (opCode == 0) // Hello
                    {
                        _logger.LogInformation("OBS v5 Hello received");
                        _isObsV5 = true;
                        var rpcVersionToken = data?["rpcVersion"];
                        var rpcVersion = rpcVersionToken != null ? (int)rpcVersionToken : -1; // Provide a default value
                        if (data?["authentication"] is JObject authObject)
                        {
                            _authChallenge = (authObject["challenge"] as JValue)?.ToString() ?? string.Empty;
                            _authSalt = (authObject["salt"] as JValue)?.ToString() ?? string.Empty;
                        }
                        Authenticate(_password);
                    }
                    else if (opCode == 2) // Identified
                    {
                        _logger.LogInformation("OBS v5 authenticated successfully");
                        OnConnection(msg);
                    }
                }
                else
                {
                    // Handle older OBS websocket versions if needed
                    var updateType = jsonObject["update-type"]?.ToString();
                    if (updateType != null)
                    {
                        if (updateType == "AuthenticationSuccess")
                        {
                            _logger.LogInformation("OBS v4 authenticated successfully");
                            OnConnection(msg);
                        }
                        _logger.LogDebug("OBS Event: {UpdateType}", updateType);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error parsing OBS message: {Message}", ex.Message);
            }
        }

        private void OnDisconnection(object info)
        {
            // For now, just log that disconnection happened
            _logger.LogInformation("Disconnected from OBS");

            _isConnected = false;
            _connectionState = ObsConnectionState.OBS_DISCONNECTED;

            if (_shouldReconnect)
            {
                StartReconnectLoop();
            }
        }

        public bool SetScene(string sceneName)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning)
            {
                _logger.LogWarning("Cannot set scene: Not connected to OBS");
                return false;
            }

            var request = new
            {
                op = 1,
                d = new
                {
                    requestType = "SetCurrentProgramScene",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new { sceneName = sceneName }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _logger.LogDebug("Sending SetScene request: {RequestJson}", requestJson);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceVisibility(string sourceName, bool visible)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning)
            {
                _logger.LogWarning("Cannot set source visibility: Not connected to OBS");
                return false;
            }

            // Use a standard scene name that should exist in OBS by default
            var sceneName = "Scene"; // Default OBS scene name

            // Try to use SetSceneItemEnabled with source name directly (OBS WebSocket 5.x approach)
            // This approach doesn't require knowing the sceneItemId in advance
            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetSceneItemEnabled",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sceneName = sceneName,
                        sourceName = sourceName, // Use source name instead of sceneItemId
                        sceneItemEnabled = visible
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _logger.LogDebug("Sending SetSceneItemEnabled request: {RequestJson}", requestJson);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool PlayMedia(string mediaSourceName, string filePath)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning)
            {
                _logger.LogWarning("Cannot play media: Not connected to OBS");
                return false;
            }

            // This is a complex operation in OBS v5 and might require a sequence of requests.
            // For simplicity, we assume the media source is already configured.

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "TriggerMediaInputAction",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        inputName = mediaSourceName,
                        mediaAction = "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PLAY"
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _logger.LogDebug("Sending PlayMedia request: {RequestJson}", requestJson);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool StopMedia(string mediaSourceName)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "TriggerMediaInputAction",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        inputName = mediaSourceName,
                        mediaAction = "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_STOP"
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceProperties(string sourceName, object properties)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetInputSettings",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        inputName = sourceName,
                        inputSettings = properties
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceTransform(string sourceName, Transform transform)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;
            var sceneName = "YOUR_SCENE_NAME"; // You need to get the scene name
            var sceneItemId = -1; // You need to get the scene item id

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetSceneItemTransform",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sceneName = sceneName,
                        sceneItemId = sceneItemId,
                        sceneItemTransform = new
                        {
                            positionX = transform.X,
                            positionY = transform.Y,
                            scaleX = transform.Width / 1920.0, // Assuming base width of 1920
                            scaleY = transform.Height / 1080.0, // Assuming base height of 1080
                            rotation = transform.Rotation,
                            boundsType = "OBS_BOUNDS_SCALE_INNER",
                            boundsAlignment = 0,
                            boundsWidth = transform.Width,
                            boundsHeight = transform.Height
                        }
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceCrop(string sourceName, int top, int bottom, int left, int right)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var sceneName = "YOUR_SCENE_NAME"; // You need to get the scene name
            var sceneItemId = -1; // You need to get the scene item id

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetSceneItemTransform",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sceneName = sceneName,
                        sceneItemId = sceneItemId,
                        sceneItemTransform = new
                        {
                            cropTop = top,
                            cropBottom = bottom,
                            cropLeft = left,
                            cropRight = right
                        }
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool CreateSource(string sourceName, string sourceType, string sceneName, object settings)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning)
            {
                _logger.LogWarning("Cannot create source: Not connected to OBS");
                return false;
            }

            // Log the settings being passed
            var settingsJson = Newtonsoft.Json.JsonConvert.SerializeObject(settings);
            _logger.LogDebug("Creating source '{SourceName}' of type '{SourceType}' in scene '{SceneName}' with settings: {Settings}",
                sourceName, sourceType, sceneName, settingsJson);

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "CreateInput",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sceneName = sceneName,
                        inputName = sourceName,
                        inputKind = sourceType,
                        inputSettings = settings,
                        sceneItemEnabled = true
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _logger.LogDebug("Sending CreateInput request: {RequestJson}", requestJson);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool DeleteSource(string sourceName, string sceneName)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;
            var sceneItemId = -1; // You need to get the scene item id

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "RemoveSceneItem",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sceneName = sceneName,
                        sceneItemId = sceneItemId
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceFilterVisibility(string sourceName, string filterName, bool visible)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetSourceFilterEnabled",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sourceName = sourceName,
                        filterName = filterName,
                        filterEnabled = visible
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetSourceFilterSettings(string sourceName, string filterName, object settings)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetSourceFilterSettings",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sourceName = sourceName,
                        filterName = filterName,
                        filterSettings = settings
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool SetMediaTime(string mediaSourceName, double timeSeconds)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "SetMediaTime",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sourceName = mediaSourceName,
                        timestamp = timeSeconds * 1000 // Convert to milliseconds
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public bool GetMediaTime(string mediaSourceName)
        {
            if (!_isConnected || _websocketClient == null || !_websocketClient.IsRunning) return false;

            var request = new
            {
                op = 6,
                d = new
                {
                    requestType = "GetMediaTime",
                    requestId = Guid.NewGuid().ToString(),
                    requestData = new
                    {
                        sourceName = mediaSourceName
                    }
                }
            };

            var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
            _websocketClient.Send(requestJson);

            return true;
        }

        public async Task DisconnectAsync()
        {
            // Stop reconnection attempts
            _shouldReconnect = false;
            StopReconnectLoop();
            _reconnectTimer?.Dispose();
            _reconnectTimer = null;

            if (_websocketClient != null && _isConnected)
            {
                await _websocketClient.Stop(System.Net.WebSockets.WebSocketCloseStatus.NormalClosure, "User requested disconnect");
                _isConnected = false;
                _connectionState = ObsConnectionState.OBS_DISCONNECTED;
            }
        }

        public void StartAutoReconnect()
        {
            _shouldReconnect = true;
            if (_connectionState == ObsConnectionState.OBS_DISCONNECTED)
            {
                StartReconnectLoop();
            }
        }

        public void StopAutoReconnect()
        {
            _shouldReconnect = false;
            StopReconnectLoop();
            _reconnectTimer?.Dispose();
            _reconnectTimer = null;
        }

        private void StartReconnectLoop()
        {
            if (_reconnectTask != null && !_reconnectTask.IsCompleted)
                return;

            _reconnectCts = new CancellationTokenSource();

            _reconnectTask = Task.Run(async () =>
            {
                while (!_reconnectCts.Token.IsCancellationRequested)
                {
                    if (_connectionState == ObsConnectionState.OBS_CONNECTED)
                        break;

                    try
                    {
                        _connectionState = ObsConnectionState.OBS_CONNECTING;
                        _logger.LogInformation("OBS_RECONNECT_ATTEMPT");
                        await ConnectToObsAsync(_host, _port, _password);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning("OBS_RECONNECT_FAILED {msg}", ex.Message);
                    }

                    await Task.Delay(TimeSpan.FromSeconds(5), _reconnectCts.Token);
                }
            });
        }

        private void StopReconnectLoop()
        {
            _reconnectCts?.Cancel();
            _reconnectTask = null;
        }

        private void StartReconnectionTimer()
        {
            // Use the new reconnect loop instead of timer
            StartReconnectLoop();
        }

        private void Authenticate(string password)
        {
            if (_isObsV5)
            {
                if (string.IsNullOrEmpty(password) || string.IsNullOrEmpty(_authChallenge) || string.IsNullOrEmpty(_authSalt))
                {
                    _logger.LogWarning("OBS v5 authentication required, but no password provided.");
                    return;
                }

                var secret = Convert.ToBase64String(SHA256.Create().ComputeHash(Encoding.UTF8.GetBytes(password + _authSalt)));
                var authResponse = Convert.ToBase64String(SHA256.Create().ComputeHash(Encoding.UTF8.GetBytes(secret + _authChallenge)));

                var request = new
                {
                    op = 1,
                    d = new
                    {
                        rpcVersion = 1,
                        authentication = authResponse
                    }
                };
                var requestJson = Newtonsoft.Json.JsonConvert.SerializeObject(request);
                if (_websocketClient != null)
                {
                    _websocketClient.Send(requestJson);
                }
                else
                {
                    _logger.LogError("WebSocket client is null, cannot send authentication request.");
                }
            }
            else
            {
                // Handle v4 authentication if needed
            }
        }
    }
}