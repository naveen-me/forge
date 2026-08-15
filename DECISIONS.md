# Architecture Decisions

## ADR-001 — Everything is a layer

Status: Accepted

There is no special program/main/overlay scheduling concept.

Every visual source is a layer.

Reason:
- simpler JSON
- simpler engine
- flexible composition
- same rules for video, images, HTML, text and ads

## ADR-002 — Layer means z-index

Status: Accepted

`layer` is the numeric z-order.

Higher values render above lower values.

Do not introduce a separate `slot` field.

## ADR-003 — One global playout clock

Status: Accepted

All `start`/`end` values refer to one global timeline.

Reason:
- deterministic behavior
- no conflicting media clocks at the scene level
- simple scheduling
- easy hot updates

## ADR-004 — Same layer uses replacement semantics

Status: Accepted

If multiple items overlap on the same layer, the later-starting item replaces the earlier item.

Different layers may overlap freely.

## ADR-005 — Hot updates

Status: Accepted

Layer state can be changed without restarting the engine.

Updates are atomic and should become visible at a frame boundary.

## ADR-006 — Immediate and scheduled updates

Status: Accepted

Support both:

- immediate update
- update scheduled for an exact playout time

## ADR-007 — Opportunistic preload

Status: Accepted

Preload when there is enough lead time.

Never delay the global timeline because a source was not preloaded.

## ADR-008 — Local and URL sources

Status: Accepted

A source may be local or remote.

Supported protocol capabilities depend on the installed backend/build.

## ADR-009 — GPAC

Status: Accepted as the initial compositor/media foundation

Reason:
- mature multimedia framework
- active current development
- headless/filter operation
- CPU 2D compositor path
- timed composition
- broad media/protocol support
- LGPL licensing

Caveat:
- exact performance and WPE framebuffer integration must be proven in the POC.

## ADR-010 — WPE WebKit

Status: Accepted as the HTML engine

Reason:
- mature embedded WebKit port
- maintained by Igalia/WebKit ecosystem
- explicit server-side/headless rendering use cases
- suitable for HTML/CSS/JS graphics

Caveat:
- production frame/buffer integration must be validated.

## ADR-011 — FFmpeg/libav

Status: Accepted for media/output integration

Reason:
- mature decoding/encoding
- H.264/AAC
- muxing
- broad protocol support
- RTMP publishing

The core engine does not implement RTMP.

## ADR-012 — No screenshot-per-frame HTML pipeline

Status: Accepted

Do not use a production design that repeatedly saves HTML frames as PNG/JPEG and decodes them again.

Reason:
- excessive CPU
- memory bandwidth
- latency
- disk/IPC overhead

Use an offscreen frame/buffer integration.

## ADR-013 — CPU-first

Status: Accepted

The target VPS must not require a discrete GPU.

Optional hardware acceleration can be added later.

## ADR-014 — StreamKit is not a dependency

Status: Rejected

StreamKit was investigated but is too immature to be the production foundation.

It may be useful for research only.

## ADR-015 — OBS is not the engine

Status: Rejected as the server core

OBS is an excellent reference for scene concepts but is not the target runtime because the product must run headlessly on ordinary VPS infrastructure without a GPU dependency.

## ADR-016 — POC before full implementation

Status: Mandatory

The WPE -> frame -> GPAC CPU compositor path must be proven before implementing the full engine.

This is the primary architectural risk gate.
