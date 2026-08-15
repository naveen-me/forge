# Initial JSON Model

This document describes the intentionally simple user-facing configuration.

## Minimal scene

```json
{
  "canvas": {
    "width": 1920,
    "height": 1080,
    "fps": 30
  },
  "output": {
    "url": "rtmp://server/live/key"
  },
  "layers": []
}
```

## Layer

```json
{
  "id": "logo",
  "type": "image",
  "layer": 10,
  "source": "/media/logo.png",
  "start": "00:00:00",
  "end": "00:10:00",
  "x": 1700,
  "y": 40,
  "width": 160,
  "height": 90
}
```

## Common fields

| Field | Required | Meaning |
|---|---|---|
| id | yes | Stable layer item identifier |
| type | yes | video/image/html/text/etc. |
| layer | yes | Numeric z-index |
| source | usually | Local path or URL |
| start | yes | Absolute global timeline start |
| end | recommended | Maximum lifetime |
| x | optional | Canvas X |
| y | optional | Canvas Y |
| width | optional | Render width |
| height | optional | Render height |
| opacity | optional | 0..1 |
| rotation | optional | Degrees |
| crop | optional | Crop rectangle |
| effect | optional | Effect definition |
| loop | optional | Repeat source |

## Important semantics

### Same layer

Later start replaces earlier:

```text
A layer=10 start=00:00 end=00:10
B layer=10 start=00:05 end=00:15

00:00-00:05 A
00:05-00:15 B
```

### Different layer

They coexist:

```text
video layer=0
logo layer=10
ticker layer=20
```

### No special program

There is no special program field.

### Local source

```json
"source": "/media/video.mp4"
```

### URL source

```json
"source": "https://example.com/video.mp4"
```

### HLS

```json
"source": "https://example.com/live/index.m3u8"
```

### SRT

```json
"source": "srt://example.com:9000?mode=caller"
```

### HTML

```json
{
  "type": "html",
  "source": "https://graphics.example.com/lower-third"
}
```

## Time format

User-facing:

```text
HH:MM:SS
HH:MM:SS.mmm
```

Internally use integer timestamps.

## Scheduled update

Example:

```json
{
  "executeAt": "00:10:00",
  "patch": {
    "id": "logo",
    "source": "/media/logo2.png"
  }
}
```

The exact API envelope is an implementation decision; the timeline semantics are not.
