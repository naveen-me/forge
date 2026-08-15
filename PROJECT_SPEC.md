# Project Specification — TARVA Headless Playout Engine

## 1. Purpose

Create a lightweight, headless, CPU-first, JSON-driven real-time playout engine for Linux/VPS deployment.

The engine accepts a canvas definition and a collection of timed layers. Each layer can reference media, graphics or an HTML page. The engine composites active layers in real time and sends the resulting media to an output pipeline that can publish to an externally supplied RTMP destination.

The product is intentionally narrower than OBS.

## 2. Non-goals for the initial version

Do not attempt to reproduce all of OBS.

Do not initially implement:

- desktop UI
- GPU-only rendering
- advanced 3D
- arbitrary broadcast automation systems
- a proprietary RTMP protocol implementation
- a full NLE
- a complete browser
- dozens of obscure media formats

## 3. Canvas

Example:

```json
{
  "canvas": {
    "width": 1920,
    "height": 1080,
    "fps": 30
  }
}
```

The canvas is the final compositing coordinate system.

Coordinates are pixel-based unless a later version explicitly adds normalized coordinates.

## 4. Global clock

There is exactly one playout clock.

All layer timing is evaluated against it.

The clock is independent from wall-clock time.

A layer is active when:

`current_time >= start && current_time < end`

If `end` is omitted, the layer may continue until explicitly removed, subject to the engine's schema rules.

Time representation must be deterministic. The preferred user-facing representation is `HH:MM:SS[.mmm]`.

Internally, convert to an integer time unit such as nanoseconds or microseconds.

## 5. Layers

Everything is a layer.

There is no special `program`, `main`, `overlay`, or `slot` type at the scheduling level.

Example:

```json
{
  "id": "background-video",
  "type": "video",
  "layer": 0,
  "source": "/media/program.mp4",
  "start": "00:00:00",
  "end": "00:30:00"
}
```

`layer` is the z-index.

Higher values are rendered above lower values.

## 6. Same-layer replacement

If two layer items have the same numeric `layer` and their active time ranges overlap, the later-starting item replaces the earlier item from its effective start.

Example:

```text
Layer 10:
A: 00:00 -> 00:10
B: 00:05 -> 00:15

Effective:
00:00 -> 00:05 A
00:05 -> 00:15 B
```

The earlier item's declared end does not matter after replacement.

If two items have the exact same start time, resolve deterministically using a monotonically increasing revision/sequence value.

## 7. Different layers

Different layers may overlap in time and space.

Spatial overlap is valid and expected.

The engine must not reject two different layers simply because their rectangles intersect.

## 8. Layer properties

Initial common properties:

- `id`
- `type`
- `layer`
- `source`
- `start`
- `end`
- `x`
- `y`
- `width`
- `height`
- `opacity`
- optional `rotation`
- optional `crop`
- optional `effect`
- optional `loop`
- optional source offset/trim

Example:

```json
{
  "id": "logo",
  "type": "image",
  "layer": 20,
  "source": "https://example.com/logo.png",
  "start": "00:00:00",
  "end": "01:00:00",
  "x": 1740,
  "y": 40,
  "width": 140,
  "height": 80,
  "opacity": 1
}
```

## 9. Source types

A source may be:

### Local

```text
/media/program.mp4
```

### HTTP/HTTPS media

```text
https://example.com/video.mp4
```

### HLS

```text
https://example.com/live/index.m3u8
```

### SRT

```text
srt://example.com:9000?mode=caller
```

### Other network media

RTMP/RTSP/etc. may be supported when the installed backend/build supports them.

### HTML

For an HTML layer, `source` is an HTTP/HTTPS URL rendered by WPE.

The engine must use a source resolver/backend capability check instead of assuming every URL is playable.

## 10. Preloading

Preloading is opportunistic.

If sufficient lead time exists:

```text
prepare source
-> wait
-> activate at exact timeline time
```

If insufficient lead time exists:

```text
activate at scheduled time
-> load/play source
```

The engine must not shift the global timeline merely because a source was not preloaded.

## 11. Source cache

The implementation should support caching where appropriate.

- Images may be downloaded and cached.
- Local media should not be duplicated unnecessarily.
- Streaming media should normally remain streaming.
- HTML pages should be kept in persistent WPE page contexts when beneficial.

Cache policy must be bounded and configurable.

## 12. HTML layers

HTML is a first-class layer type.

Example:

```json
{
  "id": "lower-third",
  "type": "html",
  "layer": 30,
  "source": "https://graphics.example.com/lower-third",
  "start": "00:05:00",
  "end": "00:10:00",
  "x": 100,
  "y": 780,
  "width": 1200,
  "height": 220
}
```

WPE WebKit is the intended browser engine.

The production path must not depend on saving a PNG screenshot for every frame.

HTML contexts should preferably be persistent/reusable.

## 13. HTML interaction

The design should allow a future mechanism to pass structured data into an HTML page without changing the layer model.

Possible mechanisms:

- WebSocket
- JavaScript bridge
- page query parameters
- local HTTP API

The first version may implement the simplest reliable mechanism.

## 14. Effects

Initial effects should be simple and deterministic.

Examples:

- fade
- scroll
- slide
- scale
- opacity
- rotation

Scrolling can be expressed as a time function.

Example:

`x(t) = initial_x - speed * elapsed`

Do not build a huge animation language initially.

## 15. Text

Text may initially be rendered through the selected graphics/compositing backend or via HTML.

The schema should keep text as a first-class type so the user does not need to construct HTML for simple text.

## 16. Video behavior

Video layers need:

- decoder lifecycle
- source start offset
- pause/resume where supported
- loop
- end behavior
- frame timestamp alignment
- failure handling

Example source trim:

```json
{
  "id": "clip",
  "type": "video",
  "layer": 0,
  "source": "/media/full.mp4",
  "sourceStart": "00:01:30",
  "start": "00:10:00",
  "end": "00:20:00"
}
```

This means the source starts at 1:30 when it enters the global timeline at 10:00.

## 17. Blank/gap behavior

There is no need for the user to create fake black layers for gaps.

If the bottom-most active visual area has no source, the engine should use a configurable fallback:

- black
- configured fallback image
- optional fallback color

The global clock continues.

## 18. Runtime updates

Runtime changes must not require engine restart.

Preferred API categories:

```text
PUT/PATCH /scene
POST       /layers
PATCH      /layers/{id}
DELETE     /layers/{id}
POST       /layers/{id}/show
POST       /layers/{id}/hide
POST       /schedule
GET        /status
```

A WebSocket/event interface should also be considered for low-latency updates and status events.

## 19. Immediate update

A normal patch applies as soon as safely possible, preferably at a frame boundary.

At 30fps, the nominal frame interval is ~33.3ms.

The implementation should target update-to-visible latency in the tens of milliseconds for cheap property changes.

Do not promise a hard millisecond SLA until benchmarked.

## 20. Scheduled update

A caller may request:

```json
{
  "executeAt": "00:10:00"
}
```

The engine schedules the complete change against the same global clock.

This is required for deterministic broadcast-style switching.

## 21. Atomicity

A multi-property update is applied atomically.

The renderer must never see:

```text
new x + old y + new source + old width
```

because the update was only half applied.

Use immutable snapshots, revisioned state, or equivalent.

## 22. Deletes/hide

Deleting a layer removes it from future rendering.

Hiding a layer may preserve its prepared source state so it can be shown again without unnecessary reload.

## 23. Audio

Initial implementation:

- video sources may carry audio.
- audio is part of the output pipeline.
- a global audio mix is sufficient for MVP.
- audio failure must not crash video.
- detailed ducking/mixing can be added later.

## 24. Output

The engine should produce a continuous media output.

The engine itself does not implement RTMP protocol semantics.

Use FFmpeg/libav or an equivalent output adapter for:

- H.264
- AAC where required
- muxing
- network publishing
- RTMP

The RTMP destination is supplied by configuration.

Example:

```json
{
  "output": {
    "url": "rtmp://example.com/live/stream"
  }
}
```

## 25. Output failure

If the RTMP destination becomes unavailable:

- renderer should continue where practical
- output layer should reconnect
- queues must remain bounded
- state should not be lost
- logs/status must report the failure

## 26. Recovery

Source failure must be isolated.

Potential states:

```text
LOADING
READY
PLAYING
PAUSED
ENDED
ERROR
RECONNECTING
```

The engine should expose useful state through logs/API.

## 27. Performance target

Initial target:

- 1920x1080
- 30fps
- CPU-first
- no discrete GPU requirement
- one main video plus multiple image/HTML/text layers

Benchmark at:

- 2 vCPU / 4GB
- 4 vCPU / 8GB
- 8 vCPU / 16GB if available

Do not assume the target is met. Measure.

## 28. Long-running target

After the POC, target a 24-hour soak test with:

- no memory leak trend
- no unbounded queues
- no accumulating frame delay
- no recurring decoder failures
- automatic output reconnect
- controlled CPU/RAM usage

## 29. Security

HTML and network sources are untrusted inputs.

The implementation must consider:

- SSRF
- local-file access from HTML
- arbitrary navigation
- process sandboxing
- container isolation
- resource limits
- allowed URL policies
- secrets in RTMP URLs/logs

Do not log stream keys or credentials.

## 30. Example complete scene

```json
{
  "canvas": {
    "width": 1920,
    "height": 1080,
    "fps": 30
  },

  "output": {
    "url": "rtmp://example.com/live/stream"
  },

  "layers": [
    {
      "id": "base",
      "type": "video",
      "layer": 0,
      "source": "/media/program.mp4",
      "start": "00:00:00",
      "end": "00:30:00",
      "loop": true
    },

    {
      "id": "logo-a",
      "type": "image",
      "layer": 10,
      "source": "/media/logo-a.png",
      "start": "00:00:00",
      "end": "00:10:00",
      "x": 1740,
      "y": 40,
      "width": 140,
      "height": 80
    },

    {
      "id": "logo-b",
      "type": "image",
      "layer": 10,
      "source": "https://example.com/logo-b.png",
      "start": "00:05:00",
      "end": "00:15:00",
      "x": 1740,
      "y": 40,
      "width": 140,
      "height": 80
    },

    {
      "id": "ticker",
      "type": "html",
      "layer": 20,
      "source": "https://graphics.example.com/ticker",
      "start": "00:02:00",
      "end": "00:08:00",
      "x": 100,
      "y": 900,
      "width": 1720,
      "height": 100,
      "effect": {
        "type": "scroll",
        "direction": "left",
        "speed": 120
      }
    }
  ]
}
```

Effective behavior:

- base runs continuously.
- logo-a runs 00:00-00:05.
- logo-b replaces logo-a at 00:05 because both are layer 10.
- ticker runs simultaneously at layer 20.
