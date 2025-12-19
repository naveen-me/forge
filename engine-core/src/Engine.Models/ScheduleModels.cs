using System;
using System.Collections.Generic;

namespace PlayoutEngine.Models
{
    public class ScheduleConfiguration
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Version { get; set; } = "1.0.0";
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
        public string Timezone { get; set; } = "UTC";
        public List<ScheduleSegment> Segments { get; set; } = new List<ScheduleSegment>();
        public ObsConfiguration ObsConfiguration { get; set; } = new ObsConfiguration();
    }

    public class ScheduleSegment
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public double StartTimeOffset { get; set; } // seconds from schedule start
        public double Duration { get; set; } // in seconds
        public BasicMediaAsset Media { get; set; } = new BasicMediaAsset();
        public List<OverlayElement> Overlays { get; set; } = new List<OverlayElement>();
        public AdInsertionConfig Ads { get; set; } = new AdInsertionConfig();
        public RepeatPattern? RepeatPattern { get; set; }
        public List<string>? ScheduledDays { get; set; } // monday, tuesday, etc.
    }

    public class BasicMediaAsset
    {
        public string Path { get; set; } = string.Empty;
        public bool Loop { get; set; } = false;
        public string? SourceName { get; set; } // OBS source name for this media
    }

    public class OverlayElement
    {
        public string Id { get; set; } = string.Empty;
        public string Type { get; set; } = "image"; // image, text, video
        public string Source { get; set; } = string.Empty;
        public Position Position { get; set; } = new Position();
        public Size Size { get; set; } = new Size();
        public Timing Timing { get; set; } = new Timing();
    }

    public class Position
    {
        public double X { get; set; } = 0;
        public double Y { get; set; } = 0;
        public string Unit { get; set; } = "%"; // % or pixels
    }

    public class Size
    {
        public double Width { get; set; } = 100;
        public double Height { get; set; } = 100;
        public string Unit { get; set; } = "pixels"; // pixels or %
    }

    public class Timing
    {
        public double ShowAt { get; set; } = 0; // seconds from segment start
        public double HideAt { get; set; } = 0; // seconds from segment start
    }

    public class AdInsertionConfig
    {
        public bool Enabled { get; set; } = false;
        public double MaxDuration { get; set; } = 0; // in seconds
        public List<AdInsertionPoint> InsertionPoints { get; set; } = new List<AdInsertionPoint>();
    }

    public class AdInsertionPoint
    {
        public double Offset { get; set; } // seconds from segment start
        public double Duration { get; set; } // duration of ad slot in seconds
        public string GroupId { get; set; } = string.Empty; // which ad group to use
    }

    public class RepeatPattern
    {
        public bool Enabled { get; set; } = false;
        public double Interval { get; set; } // in seconds
    }

    public class ObsConfiguration
    {
        public string Scene { get; set; } = "Scene 1";
        public OutputConfiguration Output { get; set; } = new OutputConfiguration();
    }

    public class OutputConfiguration
    {
        public string Type { get; set; } = "rtmp_output";
        public Dictionary<string, object> Settings { get; set; } = new Dictionary<string, object>();
    }
    
    // Enums for state management
    public enum PlayoutState
    {
        Stopped,
        Loading,
        Playing,
        Paused,
        Transitioning,
        AdInsertion,
        OverlayUpdate,
        Error,
        Recovery
    }

    // State models for playout reconciliation
    public class ExpectedState
    {
        public string MediaId { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public double Offset { get; set; } = 0.0;
        public List<OverlayElement> Overlays { get; set; } = new List<OverlayElement>();
        public string? SceneName { get; set; }

        public static ExpectedState CreateOverlayOnly(string mediaId)
        {
            return new ExpectedState
            {
                MediaId = mediaId,
                Path = string.Empty,
                Offset = 0.0,
                Overlays = new List<OverlayElement>() // Empty list or populate with logo overlay
            };
        }
    }

    public class ActualObsState
    {
        public string? MediaId { get; set; }
        public double Offset { get; set; }
        public string? SceneName { get; set; }
        public List<string> VisibleSources { get; set; } = new List<string>();
    }

    public class PlayoutDecision
    {
        public string Type { get; set; } = string.Empty; // "SCHEDULE", "BUFFER", "LOGO"
        public object? Content { get; set; }
        public string? MediaId { get; set; }
        public double Offset { get; set; }

        public static PlayoutDecision FromSchedule(object scheduledItem)
        {
            // Implementation may vary based on actual scheduled item structure
            return new PlayoutDecision
            {
                Type = "SCHEDULE",
                Content = scheduledItem,
                MediaId = GetMediaId(scheduledItem),
                Offset = GetOffset(scheduledItem)
            };
        }

        public static PlayoutDecision FromBuffer(BufferItem bufferItem)
        {
            return new PlayoutDecision
            {
                Type = "BUFFER",
                Content = bufferItem,
                MediaId = bufferItem.Id,
                Offset = bufferItem.Offset
            };
        }

        public static PlayoutDecision ShowLogo()
        {
            return new PlayoutDecision
            {
                Type = "LOGO",
                Content = null,
                MediaId = "GLOBAL_LOGO",
                Offset = 0.0
            };
        }

        private static string? GetMediaId(object item)
        {
            // Implementation depends on the actual structure of scheduled items
            // This is a placeholder based on typical properties
            return "scheduled_media";
        }

        private static double GetOffset(object item)
        {
            // Implementation depends on the actual structure of scheduled items
            // This is a placeholder based on typical properties
            return 0.0;
        }
    }

    public class BufferItem
    {
        public string Id { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public double Offset { get; set; } = 0.0;
    }
}