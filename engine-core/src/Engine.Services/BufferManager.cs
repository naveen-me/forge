using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public interface IBufferManager
    {
        Task InitializeAsync();
        BufferItem? GetNextBufferItem();
        BufferItem? GetCurrentBufferItem();
        void ResetBuffer();
        int BufferItemCount { get; }
        bool HasBufferItems { get; }
    }

    public class BufferManager : IBufferManager
    {
        private readonly List<BufferItem> _bufferItems = new List<BufferItem>();
        private int _currentIndex = 0;
        private readonly object _lock = new object();
        private readonly ILogger<BufferManager> _logger;
        private readonly IConfiguration _configuration;
        private readonly string _bufferDirectory;

        public BufferManager(ILogger<BufferManager> logger, IConfiguration configuration)
        {
            _logger = logger;
            _configuration = configuration;
            _bufferDirectory = _configuration.GetValue<string>("Buffer:Directory") ?? "media-assets/buffer/";
        }

        public int BufferItemCount => _bufferItems.Count;
        public bool HasBufferItems => _bufferItems.Count > 0;

        public async Task InitializeAsync()
        {
            try
            {
                _logger.LogInformation("Initializing buffer from directory: {BufferDirectory}", _bufferDirectory);
                
                // Load buffer items from directory
                if (Directory.Exists(_bufferDirectory))
                {
                    var supportedExtensions = new[] { ".mp4", ".avi", ".mov", ".mkv", ".wmv", ".flv", ".webm" };
                    var mediaFiles = Directory.GetFiles(_bufferDirectory)
                        .Where(f => supportedExtensions.Contains(Path.GetExtension(f).ToLower()))
                        .OrderBy(f => f);

                    foreach (var file in mediaFiles)
                    {
                        var bufferItem = new BufferItem
                        {
                            Id = Path.GetFileNameWithoutExtension(file),
                            Path = file,
                            Offset = 0.0 // Always start from beginning for buffer
                        };
                        
                        _bufferItems.Add(bufferItem);
                        _logger.LogInformation("Added buffer item: {ItemId} from {FilePath}", bufferItem.Id, file);
                    }
                }
                else
                {
                    _logger.LogWarning("Buffer directory does not exist: {BufferDirectory}", _bufferDirectory);
                }

                // Also load from configuration if specified
                var bufferFiles = _configuration.GetSection("Buffer:Files").Get<string[]>();
                if (bufferFiles != null)
                {
                    foreach (var file in bufferFiles)
                    {
                        if (File.Exists(file))
                        {
                            var bufferItem = new BufferItem
                            {
                                Id = Path.GetFileNameWithoutExtension(file),
                                Path = file,
                                Offset = 0.0
                            };
                            
                            _bufferItems.Add(bufferItem);
                            _logger.LogInformation("Added buffer item from config: {ItemId} from {FilePath}", bufferItem.Id, file);
                        }
                        else
                        {
                            _logger.LogWarning("Buffer file does not exist: {FilePath}", file);
                        }
                    }
                }

                _logger.LogInformation("Buffer initialized with {ItemCount} items", _bufferItems.Count);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error initializing buffer");
            }
        }

        public BufferItem? GetNextBufferItem()
        {
            lock (_lock)
            {
                if (_bufferItems.Count == 0)
                    return null;

                var item = _bufferItems[_currentIndex];
                _currentIndex = (_currentIndex + 1) % _bufferItems.Count;
                
                // Always reset offset to 0 for buffer items to ensure infinite loop
                item.Offset = 0.0;
                
                return item;
            }
        }

        public void ResetBuffer()
        {
            lock (_lock)
            {
                _currentIndex = 0;
            }
        }

        public BufferItem? GetCurrentBufferItem()
        {
            lock (_lock)
            {
                if (_bufferItems.Count == 0 || _currentIndex >= _bufferItems.Count)
                    return null;

                return _bufferItems[_currentIndex];
            }
        }
    }
}