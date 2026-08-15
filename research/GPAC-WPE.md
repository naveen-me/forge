# Research Notes — GPAC + WPE

## Purpose

Record why GPAC and WPE were selected as the initial technical foundation and what still needs empirical validation.

## GPAC

Official project:
https://github.com/gpac/gpac

Official documentation:
https://wiki.gpac.io/

Relevant areas:

- compositor filter
- filter-only/headless operation
- software 2D rasterization
- FFmpeg demux/mux integration
- HLS/live sources
- SRT/network sources

The GPAC compositor documentation describes timed composition of audio, video, text and graphics. It can run as a filter rather than only as a GUI/player. The compositor has a software path for 2D composition and can be configured not to load a graphics driver.

The project is long-lived and LGPL-licensed.

### Why it fits

- mature multimedia framework
- active development
- filter graph architecture
- headless operation
- CPU 2D composition
- timestamp-aware composition
- broad media/protocol ecosystem

### Risks

- GPAC scene/compositor concepts are richer than our simple JSON model.
- SVG/scene support is not equivalent to a full browser.
- Exact WPE frame/buffer integration must be engineered.
- Exact CPU performance must be benchmarked.
- Build options determine protocol/codec capabilities.

## WPE WebKit

Official project:
https://wpewebkit.org/

WPE is the embedded WebKit port maintained by the WebKit/Igalia ecosystem.

Relevant areas:

- server-side/headless rendering
- WPEPlatform
- headless backend
- Skia rendering
- embedded/professional video use cases

WPE documentation explicitly discusses server-side rendering and HTML overlays for video/post-production/broadcast scenarios.

### Why it fits

- real browser engine
- HTML/CSS/JavaScript
- Web APIs
- embedded/headless design
- server-side rendering use cases
- production-oriented maintenance
- CPU rendering support

### Risks

- WPE is still a browser engine and can consume significant CPU/RAM.
- HTML complexity must be controlled.
- We need a reliable offscreen frame/buffer bridge.
- Browser security/isolation matters.
- Not every Chromium-specific web API can be assumed.

## WPEPlatform

New work should prefer WPEPlatform over legacy libwpe-style assumptions.

The agent must verify the current stable API against the exact WPE version used in the build.

## Required POC

```text
WPE HTML
   -> offscreen frame/buffer
   -> GPAC CPU compositor
   -> 1920x1080/30
```

Add:

- MP4
- PNG
- HTML
- simple text/graphic

Measure:

- CPU
- RAM
- output FPS
- dropped frames
- startup latency
- frame handoff cost

## Do not use

Avoid production designs based on:

```text
HTML -> screenshot PNG -> disk -> decode -> compositor
```

for every frame.

The project must prove a direct or low-copy frame path.

## Decision

GPAC + WPE are approved for the initial POC and, if the POC passes, the full product.

The POC is a gate, not a formality.
