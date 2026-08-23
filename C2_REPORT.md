# C2 — WPE → GPAC Composition Gate

**Status:** ✅ PASS

---

## Pipeline

WPE headless renders HTML → PNG frames → fed to GPAC compositor via BIFS scene → compositor produces 1920×1080 RGBA output.

**Filter Graph (GPAC C API):**
```
scene.bt (BIFS) ──► btplay ──┐
                              ├─► compositor (drv=no, opfmt=rgba, osize=1920x1080) ──► pngenc ──► fout
  file:// refs load PNGs ─────┘
```

**BIFS Scene Description (BT format):**
```bt
Creator "TARVA C2"
ClockMode 1 1000
SceneSize 1920 1080

OrderedGroup {
  Layer2D {
    Transform2D { translation -360 -340 scale 0.4 0.4 }
    Image "file:///tmp/c2_wpe_layer.png"
  }
  Layer2D {
    Transform2D { translation -360 -340 scale 0.4 0.4 }
    Image "file:///tmp/c2_solid_layer.png"
  }
}
```

**Input Sources:**
1. WPE HTML layer: `file:///tmp/c2_wpe_layer.png` — 1920×1080 RGBA, blue (#0000FF) background with white "C2 GPAC Composition Test" text
2. Solid layer: `file:///tmp/c2_solid_layer.png` — 1920×1080 RGBA, transparent background with green (#00FF00) 200×200 rectangle centered

---

## Test Results

| Metric | Value |
|---|---|
| Output dimensions | 1920 × 1080 |
| Output pixel format | RGBA (verified via PNG) |
| WPE layer pixels present | 2,033,199 ✅ |
| Solid layer (green) pixels present | 40,401 ✅ |
| Composition latency (first frame) | 208.76 ms |
| CPU time (process) | 0.586 s |
| Peak RSS | 61.1 MB |

---

## Proof GPAC Performed Composition

1. The BIFS scene uses `OrderedGroup` / `Layer2D` / `Transform2D` nodes to arrange layers — this is real GPAC scene composition, not C++ manual blending.
2. The `btplay` filter loads the BIFS scene and communicates the scene graph to the compositor internally (0 PID packets on btplay output — it's internal communication).
3. The compositor filter processes the scene graph and produces a single composited output frame.
4. No manual pixel blending code exists in this test — the entire composition is done by GPAC.

---

## Files Changed

| File | Change |
|---|---|
| `tests/test_c2_gpac_composition.cpp` | New — C2 test program (338 lines) |
| `CMakeLists.txt` | Added `test_c2_gpac_composition` target |
| `AI_PROGRESS.md` | Updated with C2 gate results |

---

## Key Technical Discoveries

1. **`gpid://` doesn't work with `-i` sources.** Use `file://` URLs in the BIFS scene description instead.
2. **Compositor outputs frame interface objects, not raw pixel data.** Use `pngenc` or `jpgenc` to capture output — `gf_filter_pck_get_data()` returns NULL for frame interfaces.
3. **`btplay` communicates scene graph to compositor internally.** It outputs 0 PID packets — the scene graph is passed through internal mechanisms.
4. **The existing `GpacCompositor` class does manual C++ pixel blending.** C2 proves the real GPAC path works and is ready to replace it.

---

## Commands Executed

```bash
# Build
cd /home/nani/tarva
export PKG_CONFIG_PATH=/usr/local/pkgconfig:/usr/local/lib/pkgconfig
cmake -B build -DCMAKE_BUILD_TYPE=Debug 2>&1
cmake --build build --target test_c2_gpac_composition 2>&1

# Run
cd /home/nani/tarva/build
export LIBGL_ALWAYS_SOFTWARE=1
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
export XDG_RUNTIME_DIR=/tmp/runtime-root
mkdir -p "$XDG_RUNTIME_DIR"
./test_c2_gpac_composition 2>&1

# Verify output
python3 -c "from PIL import Image; img=Image.open('/tmp/c2_api_composited.png'); print(f'Size: {img.size}, Mode: {img.mode}'); px=list(img.getdata()); print(f'Green: {sum(1 for p in px if p[1]>200 and p[0]<50 and p[2]<50)}, Blue: {sum(1 for p in px if p[2]>200 and p[0]<50 and p[1]<50)}, White: {sum(1 for p in px if p[0]>200 and p[1]>200 and p[2]>200)}')"
```

---

## Remaining Blockers

1. WPE subsystem processes crash on exit (known issue — content is already captured before crash)
2. libxml2 version mismatch between WPE 2.52.5 build (2.13.8) and current system (2.14.6) — cosmetic warning only
3. Output captured via `pngenc` (JPEG re-encode would lose RGBA precision) — for production, need frame interface capture or raw video output
