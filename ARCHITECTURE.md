# Architecture

## 1. High-level system

```text
                    REST / WebSocket API
                             |
                             v
                    +------------------+
                    | Scene Controller |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    | Timeline Engine  |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    | Layer Manager    |
                    +--------+---------+
                             |
          +------------------+------------------+
          |                  |                  |
          v                  v                  v
      Video Source       Image Source       HTML Source
          |                  |                  |
       FFmpeg/GPAC          GPAC             WPE WebKit
          |                  |                  |
          +------------------+------------------+
                             |
                             v
                    +------------------+
                    | GPAC compositor  |
                    | CPU 2D path      |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    | Output/Encoder   |
                    | FFmpeg/libav     |
                    +--------+---------+
                             |
                             v
                     RTMP destination
```

## 2. Responsibilities

### Engine

Owns:

- public API
- scene model
- global clock
- layer scheduling
- same-layer replacement
- z-order
- hot updates
- source lifecycle
- preloading policy
- error state
- recovery policy
- persistence
- metrics
- configuration

### GPAC

Use for:

- media pipeline pieces where appropriate
- CPU 2D compositing
- image/graphics composition
- timestamped media processing

Do not leak GPAC-specific concepts into the public JSON unless unavoidable.

### WPE

Use for:

- HTML/CSS/JavaScript rendering
- persistent HTML graphics pages
- headless rendering
- browser-side animation and data-driven graphics

Do not use screenshot-per-frame as the production integration.

### FFmpeg/libav

Use for:

- decoding where useful
- encoding
- audio processing
- muxing
- output publishing
- protocol support

Do not implement an RTMP protocol stack in the core engine.

## 3. State model

The engine should maintain an immutable/revisioned scene snapshot.

Conceptually:

```text
API update
   |
   v
validate
   |
   v
create new scene revision
   |
   v
atomically publish revision
   |
   v
renderer sees complete revision
```

This avoids partially applied changes.

## 4. Clock model

Use one monotonic playout timeline.

Internally use integer timestamps.

Never use floating-point seconds as the authoritative state.

Wall clock may be used for:

- logs
- scheduled starts tied to real time
- metrics

but not as the rendering clock.

## 5. Frame loop

Conceptually:

```text
while running:
    t = global_clock()

    scene = current_scene_snapshot()

    active_layers = scene.layers where start <= t < end

    resolve same-layer replacement

    prepare/render sources

    compose active layers ordered by layer

    produce audio/video frame

    send to output

    publish metrics
```

The implementation may use event-driven scheduling, worker threads and queues instead of a literal loop.

## 6. Source lifecycle

Prefer:

```text
DECLARED
  |
  v
PREPARING
  |
  v
READY
  |
  v
ACTIVE
  |
  +----> ERROR
  |
  v
ENDED
```

Preload is opportunistic.

If preparation cannot complete before the scheduled time, the timeline must not move.

## 7. Same-layer resolution

For each numeric layer:

1. collect items whose time ranges include current time.
2. select the item with the greatest effective start time.
3. if starts are equal, select greatest revision/sequence.
4. render only the selected item for that layer.

This is deterministic and simple.

## 8. Z ordering

Sort selected active layers by ascending numeric `layer`.

Render in that order.

Higher layer values appear on top.

## 9. HTML integration

The preferred architecture is:

```text
WPE page
  |
  v
offscreen/headless rendering surface
  |
  v
frame/buffer bridge
  |
  v
compositor input
```

The implementation must investigate the lowest-copy practical path.

Avoid:

```text
WPE -> PNG screenshot -> disk -> decode -> compositor
```

for continuous production rendering.

## 10. API update path

```text
Client
  |
  v
HTTP/WebSocket
  |
  v
validation
  |
  v
revision builder
  |
  v
atomic scene swap
  |
  v
frame boundary
  |
  v
renderer
```

## 11. Scheduled update path

```text
client:
  executeAt = 00:10:00

engine:
  validate
  store pending operation
  timeline reaches 00:10:00
  atomically apply
  renderer sees new state
```

## 12. Backpressure

All asynchronous boundaries must be bounded.

A slow consumer must not cause infinite queues.

Choose an explicit policy:

- drop stale video frames when safe
- preserve control messages
- preserve audio timing where required
- expose counters

## 13. Process isolation

Where practical:

- WPE should run in a restricted process/container context.
- source/network handling should have timeouts.
- memory and CPU limits should be configurable.
- HTML navigation should be controlled.

## 14. Docker target

The production container should be:

- Linux
- headless
- no desktop environment
- no X11 dependency for the intended path
- no discrete GPU requirement
- minimal runtime packages
- health endpoint
- structured logs

## 15. Future extensibility

The public scene model should remain independent of rendering backend.

Potential future replacements:

```text
GPAC -> custom compositor
WPE -> another browser engine
FFmpeg -> another encoder/output adapter
```

without changing the user's JSON semantics.
