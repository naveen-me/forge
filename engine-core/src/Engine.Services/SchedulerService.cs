using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public interface ISchedulerService
    {
        Task<bool> LoadScheduleAsync(string scheduleJsonPath);
        Task StartScheduleAsync();
        Task PauseScheduleAsync();
        Task StopScheduleAsync();
        Task<bool> UpdateCurrentSegmentTimeAsync(TimeSpan newTime);
        Task<List<ScheduleSegment>> GetUpcomingSegmentsAsync(int count);
        Task<bool> LoadScheduleV2Async(string scheduleJsonPath);
        object? GetActiveItem(DateTime now);
    }

    public class SchedulerService : ISchedulerService
    {
        private ScheduleConfiguration? _currentScheduleV1;
        private TimelineConfigurationV2? _currentScheduleV2;
        private DateTime _scheduleStartTime;
        private TimeSpan _currentPlaybackPosition;
        private bool _isPlaying;

        public async Task<bool> LoadScheduleAsync(string scheduleJsonPath)
        {
            try
            {
                // Load and validate schedule from JSON (V1 format)
                string jsonString = await System.IO.File.ReadAllTextAsync(scheduleJsonPath);

                var options = new System.Text.Json.JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true,
                };

                _currentScheduleV1 = System.Text.Json.JsonSerializer.Deserialize<ScheduleConfiguration>(jsonString, options);

                // Validate schedule integrity
                if (_currentScheduleV1 == null || !ValidateScheduleV1(_currentScheduleV1))
                {
                    throw new ArgumentException("Invalid schedule configuration");
                }

                // Reset state
                _scheduleStartTime = DateTime.UtcNow;
                _currentPlaybackPosition = TimeSpan.Zero;
                _isPlaying = false;

                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error loading schedule: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> LoadScheduleV2Async(string scheduleJsonPath)
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

                _currentScheduleV2 = System.Text.Json.JsonSerializer.Deserialize<TimelineConfigurationV2>(jsonString, options);

                // Validate V2 schedule integrity
                if (_currentScheduleV2 == null || !ValidateScheduleV2(_currentScheduleV2))
                {
                    throw new ArgumentException("Invalid V2 schedule configuration");
                }

                // Reset state
                _scheduleStartTime = DateTime.UtcNow;
                _currentPlaybackPosition = TimeSpan.Zero;
                _isPlaying = false;

                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error loading V2 schedule: {ex.Message}");
                return false;
            }
        }

        public async Task StartScheduleAsync()
        {
            if (_currentScheduleV1 != null)
            {
                _isPlaying = true;
                // Start the V1 playout process
                await ExecuteScheduleV1();
            }
            else if (_currentScheduleV2 != null)
            {
                _isPlaying = true;
                // Start the V2 playout process
                await ExecuteScheduleV2();
            }
            else
            {
                throw new InvalidOperationException("No schedule loaded");
            }
        }

        public async Task PauseScheduleAsync()
        {
            _isPlaying = false;
            // Pause execution
            await Task.Delay(10); // Allow current operation to complete
        }

        public async Task StopScheduleAsync()
        {
            _isPlaying = false;
            _currentPlaybackPosition = TimeSpan.Zero;
            // Stop execution
            await Task.Delay(10); // Allow current operation to complete
        }

        public async Task<bool> UpdateCurrentSegmentTimeAsync(TimeSpan newTime)
        {
            if (_currentScheduleV1?.Segments != null)
            {
                _currentPlaybackPosition = newTime;
                return true;
            }
            else if (_currentScheduleV2 != null)
            {
                _currentPlaybackPosition = newTime;
                return true;
            }

            return false;
        }

        public async Task<List<ScheduleSegment>> GetUpcomingSegmentsAsync(int count)
        {
            var upcoming = new List<ScheduleSegment>();
            if (_currentScheduleV1?.Segments == null) return upcoming;

            var currentTime = DateTime.UtcNow - _scheduleStartTime + _currentPlaybackPosition;

            // Find segments that occur after current time
            foreach (var segment in _currentScheduleV1.Segments)
            {
                var segmentStart = TimeSpan.FromSeconds(segment.StartTimeOffset);
                if (segmentStart > currentTime && upcoming.Count < count)
                {
                    upcoming.Add(segment);
                }
            }

            return upcoming;
        }

        private bool ValidateScheduleV1(ScheduleConfiguration schedule)
        {
            // Perform validation checks for V1
            if (string.IsNullOrEmpty(schedule.Id)) return false;
            if (schedule.Segments == null || schedule.Segments.Count == 0) return false;

            // Check for overlapping segments
            if (HasOverlappingSegments(schedule.Segments)) return false;

            return true;
        }

        private bool ValidateScheduleV2(TimelineConfigurationV2 schedule)
        {
            // Perform validation checks for V2
            if (schedule.SchemaVersion != "2.0") return false;
            if (schedule.Timeline?.Tracks == null || schedule.Timeline.Tracks.Count == 0) return false;

            // Validate canvas settings
            if (schedule.Canvas.Width < 640 || schedule.Canvas.Height < 480) return false;

            return true;
        }

        private bool HasOverlappingSegments(List<ScheduleSegment> segments)
        {
            // Sort segments by start time
            var sortedSegments = segments.OrderBy(s => s.StartTimeOffset).ToList();

            for (int i = 0; i < sortedSegments.Count - 1; i++)
            {
                var currentEnd = sortedSegments[i].StartTimeOffset + sortedSegments[i].Duration;
                var nextStart = sortedSegments[i + 1].StartTimeOffset;

                if (currentEnd > nextStart)
                {
                    return true; // Overlap detected
                }
            }

            return false;
        }

        private async Task ExecuteScheduleV1()
        {
            if (_currentScheduleV1?.Segments == null) return;

            var sortedSegments = _currentScheduleV1.Segments.OrderBy(s => s.StartTimeOffset).ToList();

            foreach (var segment in sortedSegments)
            {
                if (!_isPlaying) break; // Check if still playing

                // Calculate when this segment should start
                var segmentStartOffset = TimeSpan.FromSeconds(segment.StartTimeOffset);
                var timeUntilStart = segmentStartOffset - _currentPlaybackPosition;

                if (timeUntilStart > TimeSpan.Zero)
                {
                    // Wait until segment start time
                    await Task.Delay(timeUntilStart);
                }

                // Execute the segment
                await ExecuteSegmentV1(segment);
            }
        }

        private async Task ExecuteSegmentV1(ScheduleSegment segment)
        {
            Console.WriteLine($"Executing V1 segment: {segment.Name} at {DateTime.Now}");
            // This would typically interact with the OBS service
        }

        private async Task ExecuteScheduleV2()
        {
            if (_currentScheduleV2?.Timeline?.Tracks == null) return;

            // Sort all timeline items across all tracks by start time
            var allItems = new List<(double time, TimelineItem item, string trackType)>();

            foreach (var track in _currentScheduleV2.Timeline.Tracks)
            {
                foreach (var item in track.Items)
                {
                    if (item is ProgramTimelineItem progItem)
                    {
                        allItems.Add((progItem.Timeline.Start, item, track.Type));
                        if (progItem.Timeline.End > progItem.Timeline.Start)
                        {
                            allItems.Add((progItem.Timeline.End, item, track.Type + "_END"));
                        }
                    }
                    else if (item is AdTimelineItem adItem)
                    {
                        allItems.Add((adItem.InsertAt, item, track.Type));
                        // Calculate end time based on duration from ad asset
                        var adAsset = _currentScheduleV2.Assets.Ads.FirstOrDefault(a => a.Id == adItem.AssetRef);
                        if (adAsset != null)
                        {
                            allItems.Add((adItem.InsertAt + adAsset.Duration, item, track.Type + "_END"));
                        }
                    }
                    else if (item is OverlayTimelineItem overlayItem)
                    {
                        allItems.Add((overlayItem.Timeline.Start, item, track.Type));
                        if (overlayItem.Timeline.End > overlayItem.Timeline.Start)
                        {
                            allItems.Add((overlayItem.Timeline.End, item, track.Type + "_END"));
                        }
                    }
                    else if (item is LiveTimelineItem liveItem)
                    {
                        allItems.Add((liveItem.Timeline.Start, item, track.Type));
                        if (liveItem.Timeline.End > liveItem.Timeline.Start)
                        {
                            allItems.Add((liveItem.Timeline.End, item, track.Type + "_END"));
                        }
                    }
                }
            }

            // Sort items by time
            var sortedItems = allItems.OrderBy(x => x.time).ToList();

            foreach (var (time, item, trackType) in sortedItems)
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
                await ExecuteTimelineItem(item, trackType);
            }
        }

        private async Task ExecuteTimelineItem(TimelineItem item, string trackType)
        {
            switch (item)
            {
                case ProgramTimelineItem progItem:
                    Console.WriteLine($"Executing program item: {progItem.Id} at {DateTime.Now}");
                    // Handle program media with transform and offset
                    break;
                case AdTimelineItem adItem:
                    Console.WriteLine($"Executing ad item: {adItem.Id} at {DateTime.Now}");
                    // Handle ad insertion with resume functionality
                    break;
                case OverlayTimelineItem overlayItem:
                    Console.WriteLine($"Executing overlay item: {overlayItem.Id} at {DateTime.Now}");
                    // Handle overlay with scope (global/program-specific)
                    break;
                case LiveTimelineItem liveItem:
                    Console.WriteLine($"Executing live item: {liveItem.Id} at {DateTime.Now}");
                    // Handle live browser source
                    break;
            }
        }

        public object? GetActiveItem(DateTime now)
        {
            if (_currentScheduleV1?.Segments != null)
            {
                var elapsed = (now - _scheduleStartTime + _currentPlaybackPosition).TotalSeconds;

                // Find the active segment based on elapsed time
                foreach (var segment in _currentScheduleV1.Segments)
                {
                    var segmentStart = segment.StartTimeOffset;
                    var segmentEnd = segmentStart + segment.Duration;

                    if (elapsed >= segmentStart && elapsed < segmentEnd)
                    {
                        return segment;
                    }
                }
            }

            return null;
        }
    }
}