# Initial API Contract

The exact HTTP framework is an implementation decision.

## Health

```http
GET /health
```

Returns process/engine health.

## Status

```http
GET /status
```

Should expose:

- running state
- current playout time
- FPS
- dropped frames
- active layers
- source states
- output state
- CPU/RAM metrics where available

## Full scene

```http
PUT /scene
```

Replaces the complete scene atomically after validation.

## Add layer

```http
POST /layers
```

Adds a layer item.

## Patch layer

```http
PATCH /layers/{id}
```

Changes only specified properties.

Example:

```json
{
  "x": 500,
  "y": 800,
  "opacity": 0.8
}
```

## Delete layer

```http
DELETE /layers/{id}
```

Removes future rendering of that item.

## Hide/show

```http
POST /layers/{id}/hide
POST /layers/{id}/show
```

Implementation may preserve prepared state.

## Scheduled operation

```http
POST /schedule
```

Example:

```json
{
  "executeAt": "00:10:00",
  "operation": {
    "type": "patch-layer",
    "id": "logo",
    "patch": {
      "source": "/media/logo2.png"
    }
  }
}
```

## WebSocket

A WebSocket interface is recommended for:

- state updates
- low-latency control
- status events
- errors
- source state changes
- output state changes

## API guarantees

- validate before mutating scene state
- atomic state changes
- deterministic ordering
- no restart for ordinary layer updates
- clear errors
- never log secrets
