using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Services;
using PlayoutEngine.Models;
using PlayoutEngine.OBS;

namespace PlayoutEngine.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class StatusController : ControllerBase
    {
        private readonly PlayoutEngineService _engineService;
        private readonly IObsIntegrationService _obsService;
        private readonly ILogger<StatusController> _logger;

        public StatusController(
            PlayoutEngineService engineService,
            IObsIntegrationService obsService,
            ILogger<StatusController> logger)
        {
            _engineService = engineService;
            _obsService = obsService;
            _logger = logger;
        }

        [HttpGet]
        public ActionResult<EngineStatus> GetStatus()
        {
            try
            {
                var currentState = _engineService.GetCurrentState();
                var obsState = _obsService.ConnectionState;

                var status = new EngineStatus
                {
                    State = currentState.ToString(),
                    ObsConnected = obsState == ObsConnectionState.OBS_CONNECTED,
                    ObsState = obsState.ToString(),
                    ScheduleLoaded = !string.IsNullOrEmpty(_engineService.GetCurrentSchedulePath()),
                    BufferItemCount = _engineService.GetBufferItemCount(),
                    Timestamp = DateTime.UtcNow
                };

                return Ok(status);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting engine status");
                return StatusCode(500, new { error = "Failed to retrieve engine status" });
            }
        }

        [HttpPost("start")]
        public async Task<ActionResult> StartPlayout()
        {
            try
            {
                var result = await _engineService.StartPlayout();
                if (result)
                {
                    return Ok(new { message = "Playout started successfully" });
                }
                else
                {
                    return BadRequest(new { error = "Failed to start playout" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error starting playout");
                return StatusCode(500, new { error = "Failed to start playout" });
            }
        }

        [HttpPost("pause")]
        public async Task<ActionResult> PausePlayout()
        {
            try
            {
                var result = await _engineService.PausePlayout();
                if (result)
                {
                    return Ok(new { message = "Playout paused successfully" });
                }
                else
                {
                    return BadRequest(new { error = "Failed to pause playout" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error pausing playout");
                return StatusCode(500, new { error = "Failed to pause playout" });
            }
        }

        [HttpPost("stop")]
        public async Task<ActionResult> StopPlayout()
        {
            try
            {
                var result = await _engineService.StopPlayout();
                if (result)
                {
                    return Ok(new { message = "Playout stopped successfully" });
                }
                else
                {
                    return BadRequest(new { error = "Failed to stop playout" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error stopping playout");
                return StatusCode(500, new { error = "Failed to stop playout" });
            }
        }

        [HttpGet("current-item")]
        public ActionResult<CurrentItemInfo> GetCurrentItem()
        {
            try
            {
                var currentState = _engineService.GetCurrentState();
                var decision = _engineService.GetCurrentDecision(DateTime.Now);

                var currentItem = new CurrentItemInfo
                {
                    State = currentState.ToString(),
                    Type = decision?.Type ?? "IDLE",
                    Content = decision?.Content?.ToString() ?? "No content",
                    MediaId = decision?.MediaId ?? "N/A",
                    Offset = decision?.Offset ?? 0.0,
                    Timestamp = DateTime.UtcNow
                };

                return Ok(currentItem);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting current item");
                return StatusCode(500, new { error = "Failed to retrieve current item" });
            }
        }
    }

    public class EngineStatus
    {
        public string State { get; set; } = string.Empty;
        public bool ObsConnected { get; set; }
        public string ObsState { get; set; } = string.Empty;
        public bool ScheduleLoaded { get; set; }
        public int BufferItemCount { get; set; }
        public DateTime Timestamp { get; set; }
    }

    public class CurrentItemInfo
    {
        public string State { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        public string MediaId { get; set; } = string.Empty;
        public double Offset { get; set; }
        public DateTime Timestamp { get; set; }
    }
}