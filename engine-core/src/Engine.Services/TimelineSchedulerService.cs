using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using PlayoutEngine.Models;
using PlayoutEngine.OBS;

namespace PlayoutEngine.Services
{
    public interface ITimelineSchedulerService
    {
        Task<bool> LoadTimelineScheduleAsync(string scheduleJsonPath);
        Task StartScheduleAsync();
        Task PauseScheduleAsync();
        Task StopScheduleAsync();
        Task<bool> UpdateCurrentTimeAsync(TimeSpan newTime);
    }

    public class TimelineSchedulerService : ITimelineSchedulerService
    {
        private TimelineConfigurationV2? _currentSchedule;
        private DateTime _scheduleStartTime;
        private TimeSpan _currentPlaybackPosition;
        private bool _isPlaying;
        private readonly IObsIntegrationService _obsService;
        private readonly Dictionary<string, string> _activeSourceMap = new Dictionary<string, string>();
        private string _currentScene = "Broadcast Scene";

        public TimelineSchedulerService(IObsIntegrationService obsService)
        {
            _obsService = obsService;
        }

        public async Task<bool> LoadTimelineScheduleAsync(string scheduleJsonPath)
        {
            try
            {
                // Load and validate V2 schedule from JSON
                string jsonString = await System.IO.File.ReadAllTextAsync(scheduleJsonPath);

                var options = new System.Text.Json.JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true,
                };

                // Add custom converters for polymorphic types
                options.Converters.Add(new TimelineItemConverterFactory());

                _currentSchedule = System.Text.Json.JsonSerializer.Deserialize<TimelineConfigurationV2>(jsonString, options);

                // Validate V2 schedule integrity
                if (_currentSchedule == null || !ValidateSchedule(_currentSchedule))
                {
                    throw new ArgumentException("Invalid V2 schedule configuration");
                }

                // Reset state
                _scheduleStartTime = DateTime.UtcNow;
                _currentPlaybackPosition = TimeSpan.Zero;
                _isPlaying = false;
                
                // Set up scenes and sources in OBS based on canvas settings
                await SetupObsEnvironment();
                
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error loading V2 timeline schedule: {ex.Message}");
                return false;
            }
        }

        private async Task SetupObsEnvironment()
        {
            if (_currentSchedule == null) return;

            // Create or ensure the main scene exists
            _obsService.SetScene(_currentScene);

            // Pre-create sources for all assets if needed
            foreach (var mediaAsset in _currentSchedule.Assets.Media)
            {
                var settings = new { 
                    is_local_file = true, 
                    local_file = mediaAsset.Path,
                    looping = mediaAsset.ObsProperties?.Loop ?? false
                };
                
                _obsService.CreateSource(
                    mediaAsset.Id, 
                    "ffmpeg_source", 
                    _currentScene, 
                    settings);
            }

            foreach (var liveAsset in _currentSchedule.Assets.Live)
            {
                var settings = new { 
                    url = liveAsset.Url,
                    width = liveAsset.BrowserSettings?.Width ?? 1920,
                    height = liveAsset.BrowserSettings?.Height ?? 1080,
                    fps = liveAsset.BrowserSettings?.Fps ?? 30,
                    shutdown = liveAsset.BrowserSettings?.ShutdownWhenHidden ?? true
                };
                
                _obsService.CreateSource(
                    liveAsset.Id, 
                    "browser_source", 
                    _currentScene, 
                    settings);
            }

            foreach (var overlayAsset in _currentSchedule.Assets.Overlays)
            {
                string sourceType = overlayAsset.OverlayType.ToLower() switch
                {
                    "image" => "image_source",
                    "video" => "ffmpeg_source",
                    "text" => "text_gdiplus", // OBS has two text source types
                    _ => "image_source"
                };

                object settings = overlayAsset.OverlayType.ToLower() switch
                {
                    "image" => new { file = overlayAsset.Path },
                    "video" => new { 
                        is_local_file = true, 
                        local_file = overlayAsset.Path,
                        looping = false
                    },
                    "text" => new { text = overlayAsset.Name }, // Using name as default text
                    _ => new { file = overlayAsset.Path }
                };
                
                _obsService.CreateSource(
                    overlayAsset.Id, 
                    sourceType, 
                    _currentScene, 
                    settings);
                
                // Set initial transform for overlays
                _obsService.SetSourceTransform(overlayAsset.Id, overlayAsset.DefaultTransform);
            }
        }

        public async Task StartScheduleAsync()
        {
            if (_currentSchedule == null)
            {
                throw new InvalidOperationException("No schedule loaded");
            }

            _isPlaying = true;
            await ExecuteTimelineSchedule();
        }

        public async Task PauseScheduleAsync()
        {
            _isPlaying = false;
            await Task.Delay(10); // Allow current operation to complete
        }

        public async Task StopScheduleAsync()
        {
            _isPlaying = false;
            _currentPlaybackPosition = TimeSpan.Zero;

            // Hide all sources when stopping
            if (_currentSchedule != null)
            {
                foreach (var asset in _currentSchedule.Assets.Media)
                {
                    _obsService.SetSourceVisibility(asset.Id, false);
                }
                foreach (var asset in _currentSchedule.Assets.Live)
                {
                    _obsService.SetSourceVisibility(asset.Id, false);
                }
                foreach (var asset in _currentSchedule.Assets.Overlays)
                {
                    _obsService.SetSourceVisibility(asset.Id, false);
                }
            }

            await Task.Delay(10); // Allow current operation to complete
        }

        public async Task<bool> UpdateCurrentTimeAsync(TimeSpan newTime)
        {
            if (_currentSchedule == null) return false;
            
            _currentPlaybackPosition = newTime;
            return true;
        }

        private bool ValidateSchedule(TimelineConfigurationV2 schedule)
        {
            // Perform validation checks for V2
            if (schedule.SchemaVersion != "2.0") return false;
            if (schedule.Timeline?.Tracks == null || schedule.Timeline.Tracks.Count == 0) return false;

            // Validate canvas settings
            if (schedule.Canvas.Width < 640 || schedule.Canvas.Height < 480) return false;

            return true;
        }

        private async Task ExecuteTimelineSchedule()
        {
            if (_currentSchedule?.Timeline?.Tracks == null) return;

            // Sort all timeline items across all tracks by start time
            var allItems = new List<(double time, TimelineItem item, string trackType, bool isEnd)>();

            foreach (var track in _currentSchedule.Timeline.Tracks)
            {
                foreach (var item in track.Items)
                {
                    if (item is ProgramTimelineItem progItem)
                    {
                        // Add start event
                        allItems.Add((progItem.Timeline.Start, item, track.Type, false));
                        
                        // Add end event if duration > 0
                        if (progItem.Timeline.End > progItem.Timeline.Start)
                        {
                            allItems.Add((progItem.Timeline.End, item, track.Type + "_END", true));
                        }
                        
                        // Add any transform changes within the duration
                        if (progItem.Transform != null)
                        {
                            // For this implementation, we'll apply transform at start
                            // In a full implementation, you would handle transform animations
                        }
                    }
                    else if (item is AdTimelineItem adItem)
                    {
                        // Add ad start event
                        allItems.Add((adItem.InsertAt, item, track.Type, false));
                        
                        // Find the ad duration to calculate end time
                        var adAsset = _currentSchedule.Assets.Ads.FirstOrDefault(a => a.Id == adItem.AssetRef);
                        if (adAsset != null)
                        {
                            allItems.Add((adItem.InsertAt + adAsset.Duration, item, track.Type + "_END", true));
                        }
                    }
                    else if (item is OverlayTimelineItem overlayItem)
                    {
                        // Add overlay start event
                        allItems.Add((overlayItem.Timeline.Start, item, track.Type, false));
                        
                        // Add overlay end event if duration > 0
                        if (overlayItem.Timeline.End > overlayItem.Timeline.Start)
                        {
                            allItems.Add((overlayItem.Timeline.End, item, track.Type + "_END", true));
                        }
                    }
                    else if (item is LiveTimelineItem liveItem)
                    {
                        // Add live source start event
                        allItems.Add((liveItem.Timeline.Start, item, track.Type, false));
                        
                        // Add live source end event if duration > 0
                        if (liveItem.Timeline.End > liveItem.Timeline.Start)
                        {
                            allItems.Add((liveItem.Timeline.End, item, track.Type + "_END", true));
                        }
                    }
                }
            }

            // Sort items by time
            var sortedItems = allItems.OrderBy(x => x.time).ToList();

            foreach (var (time, item, trackType, isEnd) in sortedItems)
            {
                if (!_isPlaying) break; // Check if still playing

                // Calculate when this item should execute
                var itemTimeOffset = TimeSpan.FromSeconds(time);
                var timeUntilExecution = itemTimeOffset - _currentPlaybackPosition;

                if (timeUntilExecution > TimeSpan.Zero)
                {
                    // Wait until item execution time
                    await Task.Delay(timeUntilExecution);
                }

                // Execute the timeline item
                await ExecuteTimelineItem(item, trackType, isEnd);
            }
        }

        private async Task ExecuteTimelineItem(TimelineItem item, string trackType, bool isEnd)
        {
            switch (item)
            {
                case ProgramTimelineItem progItem:
                    await ExecuteProgramItem(progItem, isEnd);
                    break;
                case AdTimelineItem adItem:
                    await ExecuteAdItem(adItem, isEnd);
                    break;
                case OverlayTimelineItem overlayItem:
                    await ExecuteOverlayItem(overlayItem, isEnd);
                    break;
                case LiveTimelineItem liveItem:
                    await ExecuteLiveItem(liveItem, isEnd);
                    break;
            }
        }

        private async Task ExecuteProgramItem(ProgramTimelineItem item, bool isEnd)
        {
            var asset = _currentSchedule?.Assets.Media.FirstOrDefault(m => m.Id == item.AssetRef);
            if (asset == null) return;

            if (!isEnd)
            {
                Console.WriteLine($"SHOWING program item: {item.Id} at {DateTime.Now}");
                
                // Set visibility
                _obsService.SetSourceVisibility(item.AssetRef, true);
                
                // Apply transform if specified
                if (item.Transform != null)
                {
                    _obsService.SetSourceTransform(item.AssetRef, item.Transform);
                }
                
                // Set media time if offset is specified
                if (item.Playback?.OffsetStart > 0)
                {
                    _obsService.SetMediaTime(item.AssetRef, item.Playback.OffsetStart);
                }
                
                // Start playing if needed
                if (asset.SourceType == "MEDIA_SOURCE")
                {
                    // The media should already start playing if visibility is set to true
                    // Additional play command may not be needed
                }
            }
            else
            {
                Console.WriteLine($"HIDING program item: {item.Id} at {DateTime.Now}");
                
                // Hide the source
                _obsService.SetSourceVisibility(item.AssetRef, false);
            }
        }

        private async Task ExecuteAdItem(AdTimelineItem item, bool isEnd)
        {
            var asset = _currentSchedule?.Assets.Ads.FirstOrDefault(a => a.Id == item.AssetRef);
            if (asset == null) return;

            if (!isEnd)
            {
                Console.WriteLine($"SHOWING ad item: {item.Id} at {DateTime.Now}");
                
                // Hide main program content if resume is enabled
                if (item.ResumeMainMedia)
                {
                    // In a real implementation, you'd track which program content was playing
                    // and pause it during the ad, then resume it afterward
                }
                
                // Show the ad
                _obsService.SetSourceVisibility(item.AssetRef, true);
                
                // Play the ad media
                _obsService.PlayMedia(item.AssetRef, asset.Path);
            }
            else
            {
                Console.WriteLine($"HIDING ad item: {item.Id} at {DateTime.Now}");
                
                // Hide the ad
                _obsService.SetSourceVisibility(item.AssetRef, false);
                
                // If resume is enabled, resume main content
                if (item.ResumeMainMedia)
                {
                    // In a real implementation, you'd resume the program content here
                }
            }
        }

        private async Task ExecuteOverlayItem(OverlayTimelineItem item, bool isEnd)
        {
            var asset = _currentSchedule?.Assets.Overlays.FirstOrDefault(o => o.Id == item.AssetRef);
            if (asset == null) return;

            if (!isEnd)
            {
                Console.WriteLine($"SHOWING overlay item: {item.Id} at {DateTime.Now}");
                
                // Determine visibility based on scope
                bool isVisible = true;
                
                if (item.Scope == "PROGRAM" && !string.IsNullOrEmpty(item.TargetProgramId))
                {
                    // In a real implementation, you'd check if the target program is active
                    // For now, we'll show it if it's a program-specific overlay
                    isVisible = true; // Simplified for this example
                }
                
                // Set visibility
                _obsService.SetSourceVisibility(item.AssetRef, isVisible);
                
                // Apply transform override if specified
                if (item.TransformOverride != null)
                {
                    _obsService.SetSourceTransform(item.AssetRef, item.TransformOverride);
                }
            }
            else
            {
                Console.WriteLine($"HIDING overlay item: {item.Id} at {DateTime.Now}");
                
                // Hide the overlay
                _obsService.SetSourceVisibility(item.AssetRef, false);
            }
        }

        private async Task ExecuteLiveItem(LiveTimelineItem item, bool isEnd)
        {
            var asset = _currentSchedule?.Assets.Live.FirstOrDefault(l => l.Id == item.AssetRef);
            if (asset == null) return;

            if (!isEnd)
            {
                Console.WriteLine($"SHOWING live item: {item.Id} at {DateTime.Now}");
                
                // Show the live source
                _obsService.SetSourceVisibility(item.AssetRef, true);
            }
            else
            {
                Console.WriteLine($"HIDING live item: {item.Id} at {DateTime.Now}");
                
                // Hide the live source
                _obsService.SetSourceVisibility(item.AssetRef, false);
            }
        }
    }
}