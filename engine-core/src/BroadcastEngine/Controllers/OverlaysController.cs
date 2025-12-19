using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.AspNetCore.Http;
using PlayoutEngine.Services;
using PlayoutEngine.Models;

namespace PlayoutEngine.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class OverlaysController : ControllerBase
    {
        private readonly ILogger<OverlaysController> _logger;

        public OverlaysController(ILogger<OverlaysController> logger)
        {
            _logger = logger;
        }

        [HttpGet]
        public ActionResult<List<OverlayInfo>> GetOverlays([FromQuery] string? type = null)
        {
            try
            {
                var overlaysDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets\\overlays");
                
                if (!Directory.Exists(overlaysDir))
                {
                    Directory.CreateDirectory(overlaysDir);
                }

                var supportedExtensions = new HashSet<string> { 
                    ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"
                };

                var files = Directory.GetFiles(overlaysDir, "*.*", SearchOption.TopDirectoryOnly)
                    .Where(f => supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                var overlayList = new List<OverlayInfo>();
                foreach (var file in files)
                {
                    var ext = Path.GetExtension(file).ToLower();
                    var fileInfo = new FileInfo(file);
                    
                    var overlayType = ext switch
                    {
                        ".png" or ".jpg" or ".jpeg" or ".gif" or ".bmp" => "image",
                        ".svg" => "vector",
                        _ => "unknown"
                    };

                    if (type == null || type.ToLower() == overlayType)
                    {
                        overlayList.Add(new OverlayInfo
                        {
                            Id = Path.GetFileNameWithoutExtension(file),
                            Name = Path.GetFileName(file),
                            Path = file,
                            Type = overlayType,
                            Size = fileInfo.Length,
                            Extension = ext,
                            LastModified = fileInfo.LastWriteTimeUtc
                        });
                    }
                }

                return Ok(overlayList);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting overlays list");
                return StatusCode(500, new { error = "Failed to retrieve overlays list" });
            }
        }

        [HttpGet("{id}")]
        public ActionResult<OverlayInfo> GetOverlayById(string id)
        {
            try
            {
                var overlaysDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets\\overlays");
                
                if (!Directory.Exists(overlaysDir))
                {
                    return NotFound(new { error = "Overlays directory not found" });
                }

                var supportedExtensions = new HashSet<string> { 
                    ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"
                };

                var files = Directory.GetFiles(overlaysDir, "*.*", SearchOption.TopDirectoryOnly)
                    .Where(f => Path.GetFileNameWithoutExtension(f) == id && supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                if (files.Count == 0)
                {
                    return NotFound(new { error = $"Overlay '{id}' not found" });
                }

                var file = files[0]; // Take the first match
                var fileInfo = new FileInfo(file);
                var ext = Path.GetExtension(file).ToLower();
                
                var overlayType = ext switch
                {
                    ".png" or ".jpg" or ".jpeg" or ".gif" or ".bmp" => "image",
                    ".svg" => "vector",
                    _ => "unknown"
                };

                var overlayInfo = new OverlayInfo
                {
                    Id = Path.GetFileNameWithoutExtension(file),
                    Name = Path.GetFileName(file),
                    Path = file,
                    Type = overlayType,
                    Size = fileInfo.Length,
                    Extension = ext,
                    LastModified = fileInfo.LastWriteTimeUtc
                };

                return Ok(overlayInfo);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting overlay by ID {OverlayId}", id);
                return StatusCode(500, new { error = "Failed to retrieve overlay" });
            }
        }

        [HttpPost]
        public async Task<ActionResult> UploadOverlay([FromForm] IFormFile? file, [FromForm] string? id = null)
        {
            try
            {
                if (file == null || file.Length == 0)
                {
                    return BadRequest(new { error = "No file uploaded" });
                }

                var supportedExtensions = new HashSet<string> { 
                    ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg"
                };

                var ext = Path.GetExtension(file.FileName).ToLower();
                if (!supportedExtensions.Contains(ext))
                {
                    return BadRequest(new { error = "Unsupported file type" });
                }

                var overlaysDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets\\overlays");
                if (!Directory.Exists(overlaysDir))
                {
                    Directory.CreateDirectory(overlaysDir);
                }

                var fileName = id ?? Path.GetFileNameWithoutExtension(file.FileName);
                var filePath = Path.Combine(overlaysDir, $"{fileName}{ext}");

                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await file.CopyToAsync(stream);
                }

                _logger.LogInformation("Overlay uploaded: {OverlayId}", fileName);
                return Ok(new { message = "Overlay uploaded successfully", id = fileName });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error uploading overlay");
                return StatusCode(500, new { error = "Failed to upload overlay" });
            }
        }

        [HttpDelete("{id}")]
        public ActionResult DeleteOverlay(string id)
        {
            try
            {
                var overlaysDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets\\overlays");
                var overlayFiles = Directory.GetFiles(overlaysDir, $"{id}.*")
                    .Where(f => new[] { ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg" }
                        .Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                if (overlayFiles.Count == 0)
                {
                    return NotFound(new { error = $"Overlay '{id}' not found" });
                }

                foreach (var file in overlayFiles)
                {
                    System.IO.File.Delete(file);
                }

                _logger.LogInformation("Overlay deleted: {OverlayId}", id);
                return Ok(new { message = "Overlay deleted successfully", id = id });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error deleting overlay {OverlayId}", id);
                return StatusCode(500, new { error = "Failed to delete overlay" });
            }
        }
    }

    public class OverlayInfo
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
        public string Extension { get; set; } = string.Empty;
        public long Size { get; set; }
        public DateTime LastModified { get; set; }
    }
}