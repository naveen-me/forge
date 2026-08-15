# Third-Party Dependency and Licensing Checklist

This is a tracking document, not legal advice.

## Core candidates

### GPAC

Repository:
https://github.com/gpac/gpac

License:
LGPL (verify exact version and included components at build time).

Required action:
- record exact GPAC commit/release
- retain license notices
- document modifications if any
- review dynamic/static linking implications with counsel before commercial distribution

### WPE WebKit

Project:
https://wpewebkit.org/

WPE WebKit and its dependency tree contain multiple licenses/components.

Required action:
- generate a complete dependency/license inventory from the exact build
- retain required notices
- document source availability/obligations where applicable

### FFmpeg

FFmpeg licensing depends on build configuration and enabled components.

Required action:
- record exact FFmpeg version
- record configure flags
- identify whether LGPL or GPL components are enabled
- do not accidentally enable GPL components if the product's licensing model cannot accommodate them

## Application dependencies

For every dependency added by the agent:

- name
- version/commit
- license
- purpose
- static/dynamic linkage
- source URL
- whether modified
- redistribution obligations

## Policy

No dependency should be added solely because it makes an implementation easier.

Prefer mature, well-maintained libraries with compatible licensing.

The final release must contain a generated third-party notices file.
