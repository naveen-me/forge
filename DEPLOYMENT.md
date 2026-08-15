# Deployment

## Target

Linux VPS without a discrete GPU.

Initial benchmark targets:

- 2 vCPU / 4GB
- 4 vCPU / 8GB

## Container goals

The production container should:

- run headless
- not require X11
- not require a desktop environment
- not require a discrete GPU
- expose health/status endpoints
- use bounded memory/queues
- write structured logs
- allow configuration through environment/file

## Suggested environment variables

```text
PLAYOUT_CONFIG=/etc/playout/config.json
PLAYOUT_LOG_LEVEL=info
PLAYOUT_CACHE_DIR=/var/cache/playout
PLAYOUT_DATA_DIR=/var/lib/playout
```

Names are provisional.

## Build strategy

Use a multi-stage Docker build:

```text
builder
  -> build GPAC/WPE/application as required

runtime
  -> copy only runtime dependencies
  -> run headless engine
```

The agent must document which GPAC/WPE packages and runtime libraries are required.

## No-GPU validation

The container must be tested on a machine with:

- no NVIDIA device
- no GPU passthrough
- no X server

The intended CPU compositor path must work.

## Resource controls

Support configurable:

- CPU limits
- memory limits
- cache limits
- maximum HTML contexts
- maximum concurrent source decoders
- queue sizes
- source timeouts

## Secrets

Never commit:

- RTMP keys
- SRT passphrases
- API tokens
- private URLs containing secrets

Use environment variables or secret files.

## Operational checks

Before production:

```text
GET /health
GET /status
```

must report:

- engine alive
- render loop healthy
- output state
- active source errors
- FPS
- dropped frames
