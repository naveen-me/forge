using System;
using System.Text.Json;
using System.Text.Json.Serialization;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class TimelineItemConverterFactory : JsonConverterFactory
    {
        public override bool CanConvert(Type typeToConvert)
        {
            return typeToConvert == typeof(TimelineItem) ||
                   typeof(TimelineItem).IsAssignableFrom(typeToConvert);
        }

        public override JsonConverter? CreateConverter(Type typeToConvert, JsonSerializerOptions options)
        {
            return new TimelineItemConverter();
        }
    }

    public class TimelineItemConverter : JsonConverter<TimelineItem>
    {
        public override bool CanConvert(Type typeToConvert)
        {
            return typeof(TimelineItem).IsAssignableFrom(typeToConvert);
        }

        public override TimelineItem Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            using JsonDocument doc = JsonDocument.ParseValue(ref reader);
            JsonElement root = doc.RootElement;

            // Determine the type based on properties present
            TimelineItem result;

            // Determine the type based on properties present
            // Check for ProgramTimelineItem specific properties
            if (root.TryGetProperty("Timeline", out _) && root.TryGetProperty("Playback", out _))
            {
                // This is a ProgramTimelineItem
                result = new ProgramTimelineItem
                {
                    Id = root.GetProperty("Id").GetString() ?? string.Empty,
                    AssetRef = root.GetProperty("AssetRef").GetString() ?? string.Empty
                };

                if (root.TryGetProperty("Timeline", out JsonElement timelineElement))
                {
                    ((ProgramTimelineItem)result).Timeline = JsonSerializer.Deserialize<TimelineTimeRange>(timelineElement.GetRawText(), options);
                }

                if (root.TryGetProperty("Playback", out JsonElement playbackElement))
                {
                    ((ProgramTimelineItem)result).Playback = JsonSerializer.Deserialize<PlaybackSettings>(playbackElement.GetRawText(), options);
                }

                if (root.TryGetProperty("Transform", out JsonElement transformElement))
                {
                    ((ProgramTimelineItem)result).Transform = JsonSerializer.Deserialize<Transform>(transformElement.GetRawText(), options);
                }
            }
            // Check for AdTimelineItem specific properties
            else if (root.TryGetProperty("InsertAt", out JsonElement insertAtElement))
            {
                // This is an AdTimelineItem
                result = new AdTimelineItem
                {
                    Id = root.GetProperty("Id").GetString() ?? string.Empty,
                    AssetRef = root.GetProperty("AssetRef").GetString() ?? string.Empty,
                    InsertAt = insertAtElement.GetDouble(),
                    ResumeMainMedia = root.GetProperty("ResumeMainMedia").GetBoolean()
                };
            }
            // Check for OverlayTimelineItem specific properties
            else if (root.TryGetProperty("Scope", out _))
            {
                // This is an OverlayTimelineItem
                result = new OverlayTimelineItem
                {
                    Id = root.GetProperty("Id").GetString() ?? string.Empty,
                    AssetRef = root.GetProperty("AssetRef").GetString() ?? string.Empty,
                    Scope = root.GetProperty("Scope").GetString() ?? string.Empty
                };

                if (root.TryGetProperty("TargetProgramId", out JsonElement targetIdElement))
                {
                    ((OverlayTimelineItem)result).TargetProgramId = targetIdElement.GetString();
                }

                if (root.TryGetProperty("Timeline", out JsonElement timelineElement))
                {
                    ((OverlayTimelineItem)result).Timeline = JsonSerializer.Deserialize<TimelineTimeRange>(timelineElement.GetRawText(), options);
                }

                if (root.TryGetProperty("TransformOverride", out JsonElement transformElement))
                {
                    ((OverlayTimelineItem)result).TransformOverride = JsonSerializer.Deserialize<Transform>(transformElement.GetRawText(), options);
                }
            }
            // Check for LiveTimelineItem specific properties
            else if (root.TryGetProperty("Url", out _) ||
                     (root.TryGetProperty("Timeline", out _) && !root.TryGetProperty("Playback", out _)))
            {
                // This is a LiveTimelineItem (has Timeline but not Playback, or has Url)
                result = new LiveTimelineItem
                {
                    Id = root.GetProperty("Id").GetString() ?? string.Empty,
                    AssetRef = root.GetProperty("AssetRef").GetString() ?? string.Empty
                };

                if (root.TryGetProperty("Timeline", out JsonElement timelineElement))
                {
                    ((LiveTimelineItem)result).Timeline = JsonSerializer.Deserialize<TimelineTimeRange>(timelineElement.GetRawText(), options);
                }
            }
            else
            {
                // Default to ProgramTimelineItem if we can't determine the type
                result = new ProgramTimelineItem
                {
                    Id = root.GetProperty("Id").GetString() ?? string.Empty,
                    AssetRef = root.GetProperty("AssetRef").GetString() ?? string.Empty
                };
            }

            return result;
        }

        public override void Write(Utf8JsonWriter writer, TimelineItem value, JsonSerializerOptions options)
        {
            // Determine the actual type and write accordingly
            switch (value)
            {
                case ProgramTimelineItem programItem:
                    JsonSerializer.Serialize(writer, programItem, typeof(ProgramTimelineItem), options);
                    break;
                case AdTimelineItem adItem:
                    JsonSerializer.Serialize(writer, adItem, typeof(AdTimelineItem), options);
                    break;
                case OverlayTimelineItem overlayItem:
                    JsonSerializer.Serialize(writer, overlayItem, typeof(OverlayTimelineItem), options);
                    break;
                case LiveTimelineItem liveItem:
                    JsonSerializer.Serialize(writer, liveItem, typeof(LiveTimelineItem), options);
                    break;
                default:
                    throw new NotSupportedException($"Type {value.GetType()} is not supported");
            }
        }
    }
}