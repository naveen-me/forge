using System;
using System.Collections.Generic;

namespace PlayoutEngine.Models
{
    public class TimelineConfigurationV2
    {
        [System.Text.Json.Serialization.JsonPropertyName("schemaVersion")]
        public string SchemaVersion { get; set; } = "2.0";
        public CanvasSettings Canvas { get; set; } = new CanvasSettings();
        public string Timezone { get; set; } = "UTC";
        public ScheduleInfo Schedule { get; set; } = new ScheduleInfo();
        public AssetsCollection Assets { get; set; } = new AssetsCollection();
        public TimelineDefinition Timeline { get; set; } = new TimelineDefinition();
        public EngineRules EngineRules { get; set; } = new EngineRules();
    }

    public class CanvasSettings
    {
        public int Width { get; set; } = 1920;
        public int Height { get; set; } = 1080;
        public int Fps { get; set; } = 30;
    }

    public class ScheduleInfo
    {
        public string Id { get; set; } = string.Empty;
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
    }

    public class AssetsCollection
    {
        public List<MediaAsset> Media { get; set; } = new List<MediaAsset>();
        public List<AdAsset> Ads { get; set; } = new List<AdAsset>();
        public List<OverlayAsset> Overlays { get; set; } = new List<OverlayAsset>();
        public List<LiveAsset> Live { get; set; } = new List<LiveAsset>();
    }

    public class MediaAsset
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public double Duration { get; set; }
        public string SourceType { get; set; } = "MEDIA_SOURCE"; // Always "MEDIA_SOURCE" for media
        public ObsProperties? ObsProperties { get; set; }
        public Filters? Filters { get; set; }
    }

    public class AdAsset
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Path { get; set; } = string.Empty;
        public double Duration { get; set; }
        public string SourceType { get; set; } = "MEDIA_SOURCE"; // Always "MEDIA_SOURCE" for ads
        public Filters? Filters { get; set; }
    }

    public class OverlayAsset
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string OverlayType { get; set; } = "IMAGE"; // IMAGE, VIDEO, TEXT
        public string? Path { get; set; } // Optional for TEXT overlays
        public Transform DefaultTransform { get; set; } = new Transform();
        public Filters? Filters { get; set; }
    }

    public class LiveAsset
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Url { get; set; } = string.Empty;
        public string SourceType { get; set; } = "BROWSER_SOURCE"; // Always "BROWSER_SOURCE" for live
        public BrowserSettings? BrowserSettings { get; set; }
    }

    public class ObsProperties
    {
        public bool Loop { get; set; } = false;
        public bool RestartOnActivate { get; set; } = true;
    }

    public class BrowserSettings
    {
        public int Width { get; set; } = 1920;
        public int Height { get; set; } = 1080;
        public int Fps { get; set; } = 30;
        public bool ShutdownWhenHidden { get; set; } = true;
    }

    public class TimelineDefinition
    {
        public List<TimelineTrack> Tracks { get; set; } = new List<TimelineTrack>();
    }

    public class TimelineTrack
    {
        public string Id { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty; // PROGRAM, LIVE, ADS, OVERLAY
        public int ZIndex { get; set; }
        public List<TimelineItem> Items { get; set; } = new List<TimelineItem>();
    }

    public abstract class TimelineItem
    {
        public string Id { get; set; } = string.Empty;
        public string AssetRef { get; set; } = string.Empty;
    }

    public class ProgramTimelineItem : TimelineItem
    {
        public TimelineTimeRange Timeline { get; set; } = new TimelineTimeRange();
        public PlaybackSettings? Playback { get; set; }
        public Transform? Transform { get; set; }
    }

    public class AdTimelineItem : TimelineItem
    {
        public double InsertAt { get; set; }
        public bool ResumeMainMedia { get; set; } = true;
    }

    public class OverlayTimelineItem : TimelineItem
    {
        public string Scope { get; set; } = "GLOBAL"; // GLOBAL, PROGRAM
        public string? TargetProgramId { get; set; } // Required when scope is PROGRAM
        public TimelineTimeRange Timeline { get; set; } = new TimelineTimeRange();
        public Transform? TransformOverride { get; set; }
    }

    public class LiveTimelineItem : TimelineItem
    {
        public TimelineTimeRange Timeline { get; set; } = new TimelineTimeRange();
    }

    public class TimelineTimeRange
    {
        public double Start { get; set; }
        public double End { get; set; }
    }

    public class PlaybackSettings
    {
        public double OffsetStart { get; set; } = 0;
    }

    public class Transform
    {
        public double X { get; set; } = 0;
        public double Y { get; set; } = 0;
        public double Width { get; set; } = 1920;
        public double Height { get; set; } = 1080;
        public double Rotation { get; set; } = 0;
        public double Opacity { get; set; } = 1;
        public int ZIndex { get; set; } = 0;
    }

    public class Filters
    {
        public ChromaKeyFilter? ChromaKey { get; set; }
        public ScrollFilter? Scroll { get; set; }
    }

    public class ChromaKeyFilter
    {
        public bool Enabled { get; set; } = false;
        public string Color { get; set; } = "#00FF00";
        public double Similarity { get; set; } = 0.1; // 0.0 to 1.0
    }

    public class ScrollFilter
    {
        public bool Enabled { get; set; } = false;
        public double Speed { get; set; } = 10; // pixels per second
        public string Direction { get; set; } = "HORIZONTAL"; // HORIZONTAL, VERTICAL
    }

    public class EngineRules
    {
        public bool ResumeAfterAd { get; set; } = true;
        public string OnObsDisconnect { get; set; } = "RETRY"; // RETRY, STOP, CONTINUE
        public string OnError { get; set; } = "STOP"; // STOP, CONTINUE, RETRY
    }
}