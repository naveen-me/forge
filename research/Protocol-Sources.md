# Research Notes — Media Sources

## Local media

Local files are first-class sources.

Examples:

```text
/media/program.mp4
/media/logo.png
```

The engine must resolve paths safely and expose missing-file errors clearly.

## HTTP/HTTPS

Media URLs may be supplied directly where supported by the backend.

Caching/preload is optional.

## HLS

`.m3u8` is an intended supported source type.

Example:

```text
https://example.com/live/index.m3u8
```

The exact codec/container combinations depend on the installed GPAC/FFmpeg build.

Live HLS must not be treated like a finite MP4 timeline. The source manager needs live-source semantics.

## SRT

SRT is an intended source type.

Example:

```text
srt://example.com:9000?mode=caller
```

SRT can carry live media, so source lifecycle/reconnect behavior matters.

Do not hard-code assumptions about:

- caller/listener/rendezvous mode
- encryption
- latency
- stream ID

Expose backend-specific parameters when required.

## RTMP/RTSP

May be supported as input when the selected backend/build provides them.

The engine should report capability rather than pretending every installation supports every protocol.

## HTML

HTML URL is a special source type.

Example:

```text
https://graphics.example.com/lower-third
```

It is rendered by WPE, not treated as a media URL.

## Source resolver

The engine should have a capability-aware resolver:

```text
local path
http(s) media
http(s) html
.m3u8
srt://
rtmp://
rtsp://
```

But the layer `type` remains authoritative.

For example:

```json
{
  "type": "html",
  "source": "https://example.com/page"
}
```

means browser rendering.

A URL by itself does not determine the layer type.

## Preload rule

If enough lead time exists, prepare the source.

If not, start from source at the requested timeline time.

Preload must never move the global clock.

## Failure rule

Source failure must produce an observable error state.

It must not crash unrelated layers or the entire engine.
