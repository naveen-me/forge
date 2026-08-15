# AGENTS.md — Autonomous Engineering Contract

You are the primary autonomous engineering agent for this repository.

## Mission

Build the TARVA Headless Playout Engine described in `PROJECT_SPEC.md` and `ARCHITECTURE.md`.

You are expected to:

1. Read all project documents before making architectural changes.
2. Research APIs and current upstream documentation yourself.
3. Implement code, tests, Docker setup, benchmarks and documentation.
4. Run builds/tests after meaningful changes.
5. Investigate failures rather than papering over them.
6. Keep the repository in a buildable state.
7. Record important technical discoveries in `DECISIONS.md` or a new decision record.
8. Continue through the roadmap autonomously whenever the acceptance criteria for the current gate pass.

## Architectural constraints

The approved initial architecture is:

`Our Engine -> GPAC CPU compositor + WPE HTML renderer -> FFmpeg/media output -> RTMP destination`

Do not replace GPAC, WPE or FFmpeg with another major framework merely because an API is inconvenient.

If an approved component proves technically unsuitable, you MUST:

- reproduce the problem,
- record exact evidence,
- identify alternatives,
- benchmark/validate the alternatives where practical,
- update `DECISIONS.md`,
- then choose the smallest defensible architectural change.

Do not silently switch to OBS, CasparCG, GStreamer-as-the-core-compositor, StreamKit, Chromium screenshots, or another stack.

## First task: proof of concept

Before building the full engine, prove:

1. WPE headless rendering works in the target Linux/Docker environment.
2. WPE can produce a usable frame/buffer without screenshot-to-PNG-per-frame.
3. GPAC can consume/composite that frame in its intended CPU 2D mode.
4. A video + image + HTML scene can run at 1920x1080/30fps.
5. CPU, RAM and dropped-frame metrics are captured.
6. The result remains stable for at least a meaningful soak test.

If the POC fails, stop at the gate and investigate.

## Definition of autonomous progress

Do not stop merely because code compiles.

For every milestone:

- build
- unit test
- integration test
- run the actual binary
- inspect logs
- measure performance where relevant
- document the result

## Do not create fake implementations

Never satisfy an interface with placeholder behavior while claiming the feature works.

Examples of unacceptable shortcuts:

- fake HTML rendering
- periodic screenshots used as the production HTML pipeline without explicit approval
- simulated RTMP success
- mocked media decoding in integration tests
- ignoring timestamps
- dropping source failures silently
- unbounded queues

Mocks are acceptable only in unit tests.

## Time and timeline rules

The engine has one global monotonic playout clock.

A layer's `start` and `end` are evaluated against that clock.

The agent must not introduce separate clocks for different media layers unless there is an explicit, documented reason.

## Same-layer replacement

If two active/overlapping items use the same numeric `layer`, the later effective start wins.

Example:

A: layer 10, start 00:00, end 00:10
B: layer 10, start 00:05, end 00:15

Effective result:

00:00-00:05 A
00:05-00:15 B

The underlying layer does not affect the timeline clock.

If exact same start times occur, use a deterministic sequence/version ordering.

## Different layers

Different layers may overlap freely.

Do not reject overlap merely because rectangles intersect.

## Hot updates

All runtime state updates must be atomic from the renderer's perspective.

A multi-field update must never be partially visible.

Prefer applying accepted updates at a frame boundary.

Support:

- immediate update
- scheduled update at a global timeline time
- add
- replace
- patch
- delete/hide

No restart should be required for ordinary layer changes.

## Source handling

Sources may be:

- local files
- HTTP/HTTPS media
- HLS `.m3u8`
- SRT
- RTMP/RTSP where supported by the backend/build
- HTML URL for HTML layers

Preload opportunistically when there is enough lead time.

If there is insufficient lead time, start from the source at the scheduled time rather than delaying the global timeline.

## Failure isolation

A broken image, video, HTML page, network source or decoder must not crash the whole engine.

Define explicit fallback/error states and expose them through logs/API.

## Performance

Use bounded queues.

Do not allow a slow encoder, network source or renderer to cause unlimited memory growth.

Measure:

- output FPS
- rendered FPS
- dropped frames
- CPU
- RAM
- source startup latency
- hot-update latency
- encoder latency
- RTMP reconnect behavior

## Licensing

Track licenses for all third-party dependencies.

Do not copy GPL/LGPL/BSD/MIT source code into the project without recording its license and complying with its terms.

## Final delivery

The final repository must include:

- source
- build instructions
- Dockerfile(s)
- configuration examples
- API documentation
- JSON schema
- example scenes
- tests
- benchmark scripts/results
- operational documentation
- dependency/license inventory
- known limitations

Do not declare production-ready until `ACCEPTANCE_TESTS.md` has been substantially satisfied.
