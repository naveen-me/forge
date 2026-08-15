# Acceptance Tests

## Gate A — Build

- [ ] Clean build from a fresh Linux checkout.
- [ ] Docker build succeeds.
- [ ] Unit tests pass.
- [ ] Integration test suite starts.

## Gate B — WPE

- [ ] WPE runs headlessly.
- [ ] No desktop environment is required.
- [ ] A controlled HTML page renders.
- [ ] CSS and JavaScript work.
- [ ] A page can remain alive for a long-running session.
- [ ] Rendering surface/frame acquisition is measurable.
- [ ] No screenshot-per-frame production pipeline is used.

## Gate C — GPAC CPU compositor

- [ ] GPAC compositor runs in filter/headless mode.
- [ ] Intended 2D CPU path is verified.
- [ ] No discrete GPU is required.
- [ ] Image layer composites correctly.
- [ ] Video layer composites correctly.
- [ ] HTML-derived frame composites correctly.
- [ ] Z ordering is correct.

## Gate D — 1080p30 POC

Scene:

- 1920x1080
- 30fps
- one MP4 video
- one PNG
- one WPE HTML layer
- one text/graphics element

Measure:

- CPU
- RAM
- rendered FPS
- output FPS
- dropped frames
- startup time

Result must be committed to `benchmarks/`.

## Gate E — Timeline

- [ ] One global clock.
- [ ] Start boundary is correct.
- [ ] End boundary is correct.
- [ ] Layer starts at exact intended timeline position.
- [ ] Gap does not stop clock.
- [ ] Bottom fallback works.
- [ ] Same-layer replacement works.
- [ ] Different layers coexist.
- [ ] Exact same start times resolve deterministically.

## Gate F — Sources

- [ ] Local MP4.
- [ ] Local image.
- [ ] HTTP media.
- [ ] HTTPS media.
- [ ] HLS `.m3u8`.
- [ ] SRT source.
- [ ] Unsupported source produces clear error.
- [ ] Network failure does not crash the engine.

## Gate G — Preload

- [ ] A source with sufficient lead time can preload.
- [ ] A source without sufficient lead time can start from the source.
- [ ] Timeline is not delayed by preload failure.
- [ ] Preloaded source activates cleanly.

## Gate H — Hot updates

- [ ] Add layer without restart.
- [ ] Patch layer without restart.
- [ ] Delete layer without restart.
- [ ] Hide/show without restart.
- [ ] Immediate update is atomic.
- [ ] Scheduled update executes at the global timeline position.
- [ ] A property-only update is visible within the expected frame latency.
- [ ] No partial scene state is visible.

## Gate I — Effects

- [ ] Position.
- [ ] Size.
- [ ] Opacity.
- [ ] Crop.
- [ ] Rotation.
- [ ] Fade.
- [ ] Scroll.
- [ ] Effect timing follows global clock.

## Gate J — HTML

- [ ] URL page loads.
- [ ] Local controlled HTML page loads.
- [ ] CSS renders.
- [ ] JavaScript renders.
- [ ] HTML animation follows capture timing.
- [ ] HTML page can receive application data through the selected bridge.
- [ ] HTML failure is isolated.
- [ ] HTML resource policy prevents unwanted local/network access.

## Gate K — Output

- [ ] Video encoding works.
- [ ] Audio encoding works where enabled.
- [ ] Output can be published to supplied RTMP destination.
- [ ] RTMP credentials are not logged.
- [ ] Output failure is detected.
- [ ] Output reconnect works.
- [ ] Renderer does not crash when RTMP is unavailable.

## Gate L — Reliability

- [ ] Bounded queues.
- [ ] No uncontrolled memory growth.
- [ ] Source decoder failure recovery.
- [ ] WPE recovery.
- [ ] Output recovery.
- [ ] Health endpoint.
- [ ] Metrics endpoint/logging.

## Gate M — VPS

Run on at least:

- 2 vCPU / 4GB
- 4 vCPU / 8GB

Preferred target:

- 1920x1080
- 30fps
- realistic layer count

Document actual results.

## Gate N — Soak

- [ ] 1 hour soak.
- [ ] 6 hour soak.
- [ ] 24 hour soak before production claim.

Record:
- CPU
- RAM
- output FPS
- dropped frames
- reconnect count
- errors
- source restarts

## Definition of Done

The product is not "done" because it compiles.

It is done when:

- the POC architecture is proven
- all critical acceptance gates pass
- Docker deployment works
- representative scenes run continuously
- hot updates work
- source failures are isolated
- RTMP recovery works
- benchmark results are documented
- limitations are documented
