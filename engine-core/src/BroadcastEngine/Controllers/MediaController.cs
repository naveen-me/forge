using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Services;
using PlayoutEngine.Models;

namespace PlayoutEngine.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MediaController : ControllerBase
    {
        private readonly ILogger<MediaController> _logger;

        public MediaController(ILogger<MediaController> logger)
        {
            _logger = logger;
        }

        [HttpGet]
        public ActionResult<List<MediaInfo>> GetMedia([FromQuery] string? type = null, [FromQuery] string? directory = null)
        {
            try
            {
                var mediaDir = directory ?? Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets");
                
                if (!Directory.Exists(mediaDir))
                {
                    return Ok(new List<MediaInfo>());
                }

                var supportedExtensions = new HashSet<string> { 
                    ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm", ".mp3", ".wav", ".jpg", ".jpeg", ".png", ".gif", ".bmp" 
                };

                var files = Directory.GetFiles(mediaDir, "*.*", SearchOption.AllDirectories)
                    .Where(f => supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                var mediaList = new List<MediaInfo>();
                foreach (var file in files)
                {
                    var ext = Path.GetExtension(file).ToLower();
                    var fileInfo = new FileInfo(file);
                    
                    var mediaType = ext switch
                    {
                        ".mp4" or ".avi" or ".mov" or ".mkv" or ".wmv" or ".flv" or ".webm" => "video",
                        ".mp3" or ".wav" => "audio",
                        ".jpg" or ".jpeg" or ".png" or ".gif" or ".bmp" => "image",
                        _ => "unknown"
                    };

                    if (type == null || type.ToLower() == mediaType)
                    {
                        mediaList.Add(new MediaInfo
                        {
                            Id = Path.GetFileNameWithoutExtension(file),
                            Name = Path.GetFileName(file),
                            Path = file,
                            Type = mediaType,
                            Size = fileInfo.Length,
                            Extension = ext,
                            LastModified = fileInfo.LastWriteTimeUtc
                        });
                    }
                }

                return Ok(mediaList);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting media list");
                return StatusCode(500, new { error = "Failed to retrieve media list" });
            }
        }

        [HttpGet("{id}")]
        public ActionResult<MediaInfo> GetMediaById(string id)
        {
            try
            {
                var mediaDir = Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets");
                
                if (!Directory.Exists(mediaDir))
                {
                    return NotFound(new { error = "Media directory not found" });
                }

                var supportedExtensions = new HashSet<string> { 
                    ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm", ".mp3", ".wav", ".jpg", ".jpeg", ".png", ".gif", ".bmp" 
                };

                var files = Directory.GetFiles(mediaDir, "*.*", SearchOption.AllDirectories)
                    .Where(f => Path.GetFileNameWithoutExtension(f) == id && supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                if (files.Count == 0)
                {
                    return NotFound(new { error = $"Media '{id}' not found" });
                }

                var file = files[0]; // Take the first match
                var fileInfo = new FileInfo(file);
                var ext = Path.GetExtension(file).ToLower();
                
                var mediaType = ext switch
                {
                    ".mp4" or ".avi" or ".mov" or ".mkv" or ".wmv" or ".flv" or ".webm" => "video",
                    ".mp3" or ".wav" => "audio",
                    ".jpg" or ".jpeg" or ".png" or ".gif" or ".bmp" => "image",
                    _ => "unknown"
                };

                var mediaInfo = new MediaInfo
                {
                    Id = Path.GetFileNameWithoutExtension(file),
                    Name = Path.GetFileName(file),
                    Path = file,
                    Type = mediaType,
                    Size = fileInfo.Length,
                    Extension = ext,
                    LastModified = fileInfo.LastWriteTimeUtc
                };

                return Ok(mediaInfo);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error getting media by ID {MediaId}", id);
                return StatusCode(500, new { error = "Failed to retrieve media" });
            }
        }

        [HttpPost("scan")]
        public ActionResult<List<MediaInfo>> ScanMedia([FromBody] ScanMediaRequest? request)
        {
            try
            {
                var mediaDir = request?.Directory ?? Path.Combine(AppContext.BaseDirectory, "..\\..\\..\\media-assets");
                
                if (!Directory.Exists(mediaDir))
                {
                    return NotFound(new { error = "Media directory not found" });
                }

                var supportedExtensions = new HashSet<string> { 
                    ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm", ".mp3", ".wav", ".jpg", ".jpeg", ".png", ".gif", ".bmp" 
                };

                var files = Directory.GetFiles(mediaDir, "*.*", SearchOption.AllDirectories)
                    .Where(f => supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                    .ToList();

                var mediaList = new List<MediaInfo>();
                foreach (var file in files)
                {
                    var ext = Path.GetExtension(file).ToLower();
                    var fileInfo = new FileInfo(file);
                    
                    var mediaType = ext switch
                    {
                        ".mp4" or ".avi" or ".mov" or ".mkv" or ".wmv" or ".flv" or ".webm" => "video",
                        ".mp3" or ".wav" => "audio",
                        ".jpg" or ".jpeg" or ".png" or ".gif" or ".bmp" => "image",
                        _ => "unknown"
                    };

                    mediaList.Add(new MediaInfo
                    {
                        Id = Path.GetFileNameWithoutExtension(file),
                        Name = Path.GetFileName(file),
                        Path = file,
                        Type = mediaType,
                        Size = fileInfo.Length,
                        Extension = ext,
                        LastModified = fileInfo.LastWriteTimeUtc
                    });
                }

                return Ok(mediaList);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error scanning media directory");
                return StatusCode(500, new { error = "Failed to scan media directory" });
            }
        }
    }

    public class MediaInfo
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
        public string Extension { get; set; } = string.Empty;
        public long Size { get; set; }
        public DateTime LastModified { get; set; }
    }

    public class ScanMediaRequest
    {
        public string? Directory { get; set; }
    }
}