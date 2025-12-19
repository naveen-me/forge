using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Services;
using PlayoutEngine.Models;
using System.ComponentModel.DataAnnotations;

namespace PlayoutEngine.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class SchedulesController : ControllerBase
    {
        private readonly ISchedulerService _schedulerService;
        private readonly PlayoutEngineService _engineService;
        private readonly ILogger<SchedulesController> _logger;

        public SchedulesController(
            ISchedulerService schedulerService, 
            PlayoutEngineService engineService,
            ILogger<SchedulesController> logger)
        {
            _schedulerService = schedulerService;
            _engineService = engineService;
            _logger = logger;
        }

        [HttpGet]
        public async Task<ActionResult<List<ScheduleInfo>>> GetSchedules()
        {
            try
            {
                // Get list of available schedule files
                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var scheduleFiles = Directory.GetFiles(schedulesDir, "*.json", SearchOption.TopDirectoryOnly);
                
                var schedules = new List<ScheduleInfo>();
                foreach (var file in scheduleFiles)
                {
                    var fileInfo = new FileInfo(file);
                    schedules.Add(new ScheduleInfo
                    {
                        Id = Path.GetFileNameWithoutExtension(file),
                        Name = Path.GetFileNameWithoutExtension(file),
                        Path = file,
                        Size = fileInfo.Length,
                        LastModified = fileInfo.LastWriteTimeUtc
                    });
                }

                return Ok(schedules);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting schedules list");
                return StatusCode(500, new { error = "Failed to retrieve schedules list" });
            }
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<string>> GetSchedule(string id)
        {
            try
            {
                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var schedulePath = Path.Combine(schedulesDir, $"{id}.json");

                if (!System.IO.File.Exists(schedulePath))
                {
                    return NotFound(new { error = $"Schedule '{id}' not found" });
                }

                var content = await System.IO.File.ReadAllTextAsync(schedulePath);
                return Ok(content);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting schedule {ScheduleId}", id);
                return StatusCode(500, new { error = "Failed to retrieve schedule" });
            }
        }

        [HttpPost]
        public async Task<ActionResult> CreateSchedule([FromBody] TimelineConfigurationV2 schedule)
        {
            try
            {
                if (schedule == null)
                {
                    return BadRequest(new { error = "Schedule data is required" });
                }

                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                if (!Directory.Exists(schedulesDir))
                {
                    Directory.CreateDirectory(schedulesDir);
                }

                var schedulePath = Path.Combine(schedulesDir, $"{schedule.Schedule.Id}.json");
                
                // Validate the schedule
                if (string.IsNullOrEmpty(schedule.Schedule.Id))
                {
                    return BadRequest(new { error = "Schedule ID is required" });
                }

                // Serialize and save the schedule
                var json = System.Text.Json.JsonSerializer.Serialize(schedule, new System.Text.Json.JsonSerializerOptions 
                { 
                    WriteIndented = true 
                });
                
                await System.IO.File.WriteAllTextAsync(schedulePath, json);
                
                _logger.LogInformation("Schedule created: {ScheduleId}", schedule.Schedule.Id);
                return Ok(new { message = "Schedule created successfully", id = schedule.Schedule.Id });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error creating schedule");
                return StatusCode(500, new { error = "Failed to create schedule" });
            }
        }

        [HttpPut("{id}")]
        public async Task<ActionResult> UpdateSchedule(string id, [FromBody] TimelineConfigurationV2 schedule)
        {
            try
            {
                if (schedule == null)
                {
                    return BadRequest(new { error = "Schedule data is required" });
                }

                if (schedule.Schedule.Id != id)
                {
                    return BadRequest(new { error = "Schedule ID in URL does not match ID in body" });
                }

                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var schedulePath = Path.Combine(schedulesDir, $"{id}.json");

                if (!System.IO.File.Exists(schedulePath))
                {
                    return NotFound(new { error = $"Schedule '{id}' not found" });
                }

                // Serialize and update the schedule
                var json = System.Text.Json.JsonSerializer.Serialize(schedule, new System.Text.Json.JsonSerializerOptions 
                { 
                    WriteIndented = true 
                });
                
                await System.IO.File.WriteAllTextAsync(schedulePath, json);
                
                _logger.LogInformation("Schedule updated: {ScheduleId}", id);
                return Ok(new { message = "Schedule updated successfully", id = id });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error updating schedule {ScheduleId}", id);
                return StatusCode(500, new { error = "Failed to update schedule" });
            }
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteSchedule(string id)
        {
            try
            {
                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var schedulePath = Path.Combine(schedulesDir, $"{id}.json");

                if (!System.IO.File.Exists(schedulePath))
                {
                    return NotFound(new { error = $"Schedule '{id}' not found" });
                }

                System.IO.File.Delete(schedulePath);
                
                _logger.LogInformation("Schedule deleted: {ScheduleId}", id);
                return Ok(new { message = "Schedule deleted successfully", id = id });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error deleting schedule {ScheduleId}", id);
                return StatusCode(500, new { error = "Failed to delete schedule" });
            }
        }

        [HttpPost("{id}/load")]
        public async Task<ActionResult> LoadSchedule(string id)
        {
            try
            {
                var schedulesDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\schedules");
                var schedulePath = Path.Combine(schedulesDir, $"{id}.json");

                if (!System.IO.File.Exists(schedulePath))
                {
                    return NotFound(new { error = $"Schedule '{id}' not found" });
                }

                var result = await _engineService.LoadSchedule(schedulePath);
                if (result)
                {
                    return Ok(new { message = "Schedule loaded successfully", id = id });
                }
                else
                {
                    return BadRequest(new { error = "Failed to load schedule" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error loading schedule {ScheduleId}", id);
                return StatusCode(500, new { error = "Failed to load schedule" });
            }
        }

        [HttpPost("load-file")]
        public async Task<ActionResult> LoadScheduleFile([FromBody] LoadScheduleRequest request)
        {
            try
            {
                if (string.IsNullOrEmpty(request?.FilePath))
                {
                    return BadRequest(new { error = "File path is required" });
                }

                if (!System.IO.File.Exists(request.FilePath))
                {
                    return NotFound(new { error = "Schedule file not found" });
                }

                var result = await _engineService.LoadSchedule(request.FilePath);
                if (result)
                {
                    return Ok(new { message = "Schedule loaded successfully", filePath = request.FilePath });
                }
                else
                {
                    return BadRequest(new { error = "Failed to load schedule" });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error loading schedule file {FilePath}", request?.FilePath);
                return StatusCode(500, new { error = "Failed to load schedule" });
            }
        }
    }

    public class ScheduleInfo
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public long Size { get; set; }
        public DateTime LastModified { get; set; }
    }

    public class LoadScheduleRequest
    {
        public string? FilePath { get; set; }
    }
}