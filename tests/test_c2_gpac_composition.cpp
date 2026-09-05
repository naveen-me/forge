#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <chrono>
#include <vector>
#include <unistd.h>
#include <sys/resource.h>
#include <png.h>
extern "C" {
#include <gpac/filters.h>
#include <gpac/constants.h>
}

static constexpr int W = 1920;
static constexpr int H = 1080;

static bool write_png_rgba(const char* path, int w, int h, const uint8_t* rgba) {
    FILE* fp = fopen(path, "wb");
    if (!fp) return false;
    png_structp png = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    png_infop info = png_create_info_struct(png);
    if (setjmp(png_jmpbuf(png))) { fclose(fp); return false; }
    png_init_io(png, fp);
    png_set_IHDR(png, info, w, h, 8, PNG_COLOR_TYPE_RGBA, PNG_INTERLACE_NONE,
                 PNG_COMPRESSION_TYPE_DEFAULT, PNG_FILTER_TYPE_DEFAULT);
    png_write_info(png, info);
    for (int y = 0; y < h; y++) png_write_row(png, rgba + y * w * 4);
    png_write_end(png, nullptr);
    png_destroy_write_struct(&png, &info);
    fclose(fp);
    return true;
}

static bool read_png_rgba(const char* path, std::vector<uint8_t>& out, int& w, int& h) {
    FILE* fp = fopen(path, "rb");
    if (!fp) return false;
    png_structp png = png_create_read_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    png_infop info = png_create_info_struct(png);
    if (setjmp(png_jmpbuf(png))) { fclose(fp); return false; }
    png_init_io(png, fp);
    png_read_info(png, info);
    w = png_get_image_width(png, info);
    h = png_get_image_height(png, info);
    png_byte color_type = png_get_color_type(png, info);
    if (color_type == PNG_COLOR_TYPE_RGB) {
        png_set_expand(png);
    } else if (color_type == PNG_COLOR_TYPE_PALETTE) {
        png_set_palette_to_rgb(png);
    }
    if (color_type == PNG_COLOR_TYPE_GRAY && png_get_bit_depth(png, info) < 8) {
        png_set_expand_gray_1_2_4_to_8(png);
    }
    if (png_get_valid(png, info, PNG_INFO_tRNS)) {
        png_set_tRNS_to_alpha(png);
    }
    // Force RGBA output
    if (color_type != PNG_COLOR_TYPE_RGBA) {
        png_set_filler(png, 0xFF, PNG_FILLER_AFTER);
    }
    png_read_update_info(png, info);
    out.resize(w * h * 4);
    std::vector<png_bytep> rows(h);
    for (int y = 0; y < h; y++) rows[y] = out.data() + y * w * 4;
    png_read_image(png, rows.data());
    png_destroy_read_struct(&png, &info, nullptr);
    fclose(fp);
    return true;
}

// Custom sink filter that captures compositor output
struct SinkCtx {
    std::vector<uint8_t> frame;
    int width = 0, height = 0;
    bool captured = false;
};

static GF_Err sink_process(GF_Filter* filter) {
    GF_FilterPid* pid = gf_filter_get_ipid(filter, 0);
    if (!pid) return GF_OK;
    
    GF_FilterPacket* pck = gf_filter_pid_get_packet(pid);
    if (!pck) return GF_OK;
    
    SinkCtx* ctx = (SinkCtx*)gf_filter_get_udta(filter);
    if (!ctx) { gf_filter_pid_drop_packet(pid); return GF_OK; }
    
    if (!ctx->captured) {
        // Try raw data first
        u32 size = 0;
        const u8* data = gf_filter_pck_get_data(pck, &size);
        if (data && size > 0) {
            u32 pw = 0, ph = 0;
            const GF_PropertyValue* pv_w = gf_filter_pck_get_property(pck, GF_PROP_PID_WIDTH);
            const GF_PropertyValue* pv_h = gf_filter_pck_get_property(pck, GF_PROP_PID_HEIGHT);
            if (pv_w) pw = pv_w->value.uint;
            if (pv_h) ph = pv_h->value.uint;
            if (!pw) pw = W;
            if (!ph) ph = H;
            ctx->width = pw;
            ctx->height = ph;
            ctx->frame.resize(size);
            memcpy(ctx->frame.data(), data, size);
            ctx->captured = true;
            fprintf(stderr, "  [Sink] Captured raw frame: %ux%u %u bytes\n", pw, ph, size);
        } else {
            // Try frame interface
            GF_FilterFrameInterface* fi = gf_filter_pck_get_frame_interface(pck);
            if (fi && fi->get_plane) {
                const u8* plane = nullptr;
                u32 stride = 0;
                GF_Err e = fi->get_plane(fi, 0, &plane, &stride);
                if (e == GF_OK && plane) {
                    u32 pw = 0, ph = 0;
                    const GF_PropertyValue* pv_w = gf_filter_pck_get_property(pck, GF_PROP_PID_WIDTH);
                    const GF_PropertyValue* pv_h = gf_filter_pck_get_property(pck, GF_PROP_PID_HEIGHT);
                    if (pv_w) pw = pv_w->value.uint;
                    if (pv_h) ph = pv_h->value.uint;
                    if (!pw) pw = W;
                    if (!ph) ph = H;
                    ctx->width = pw;
                    ctx->height = ph;
                    ctx->frame.resize(pw * ph * 4);
                    for (u32 y = 0; y < ph; y++) {
                        memcpy(ctx->frame.data() + y * pw * 4, plane + y * stride, pw * 4);
                    }
                    ctx->captured = true;
                    fprintf(stderr, "  [Sink] Captured frame interface: %ux%u stride=%u\n", pw, ph, stride);
                }
            }
        }
    }
    
    gf_filter_pid_drop_packet(pid);
    return GF_OK;
}

int main() {
    fprintf(stderr, "========================================\n");
    fprintf(stderr, " C2 — WPE → GPAC Composition Gate\n");
    fprintf(stderr, "========================================\n\n");
    
    int64_t test_start_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();

    // Phase 1: Create test PNGs
    fprintf(stderr, "--- Phase 1: Create test PNGs ---\n");
    
    // Blue background (simulating WPE output)
    std::vector<uint8_t> wpe_frame(W * H * 4);
    for (int y = 0; y < H; y++) {
        for (int x = 0; x < W; x++) {
            int idx = (y * W + x) * 4;
            wpe_frame[idx+0] = 0;    // R
            wpe_frame[idx+1] = 0x44; // G
            wpe_frame[idx+2] = 0xaa; // B
            wpe_frame[idx+3] = 255;  // A
        }
    }
    write_png_rgba("/tmp/c2_api_wpe.png", W, H, wpe_frame.data());
    fprintf(stderr, "  WPE layer: /tmp/c2_api_wpe.png (%dx%d RGBA)\n", W, H);
    
    // Solid green rectangle with transparent background
    std::vector<uint8_t> solid_frame(W * H * 4, 0);
    for (int y = 440; y < 640; y++) {
        for (int x = 860; x < 1060; x++) {
            int idx = (y * W + x) * 4;
            solid_frame[idx+0] = 0; solid_frame[idx+1] = 255; solid_frame[idx+2] = 0; solid_frame[idx+3] = 255;
        }
    }
    write_png_rgba("/tmp/c2_api_solid.png", W, H, solid_frame.data());
    fprintf(stderr, "  Solid layer: /tmp/c2_api_solid.png (%dx%d RGBA, 200x200 green rect @ center)\n", W, H);
    
    // Phase 2: Create BIFS scene
    fprintf(stderr, "\n--- Phase 2: Create BIFS scene ---\n");
    const char* scene_path = "/tmp/c2_api_scene.bt";
    FILE* sf = fopen(scene_path, "w");
    fprintf(sf,
        "OrderedGroup {\n"
        "  children [\n"
        "    Background2D {\n"
        "      backColor 0 0 0\n"
        "      url \"/tmp/c2_api_wpe.png\"\n"
        "    }\n"
        "    Layer2D {\n"
        "      children [\n"
        "        Transform2D {\n"
        "          translation 860 440\n"
        "          children [\n"
        "            Background2D {\n"
        "              backColor 0 255 0\n"
        "              url \"/tmp/c2_api_solid.png\"\n"
        "            }\n"
        "          ]\n"
        "        }\n"
        "      ]\n"
        "    }\n"
        "  ]\n"
        "}\n");
    fclose(sf);
    fprintf(stderr, "  BIFS scene: %s\n", scene_path);
    fprintf(stderr, "  Scene: Background2D(WPE blue) + Transform2D(860,440) + Background2D(solid green)\n");
    
    // Phase 3: GPAC filter session via C API
    fprintf(stderr, "\n--- Phase 3: GPAC filter session (C API) ---\n");
    GF_Err err = GF_OK;
    GF_FilterSession* fs = gf_fs_new_defaults(0);
    if (!fs) { fprintf(stderr, "FAIL: No GPAC session\n"); return 1; }
    
    // Load BIFS scene as source (btplay filter handles .bt files)
    GF_Filter* scene_src = gf_fs_load_source(fs, scene_path, nullptr, nullptr, &err);
    fprintf(stderr, "  Scene source (btplay): %p err=%d\n", scene_src, err);
    
    // Load compositor filter - CPU 2D mode, RGBA output, 1920x1080
    GF_Filter* compositor = gf_fs_load_filter(fs, "compositor:drv=no:opfmt=rgba:fps=1:osize=1920x1080", &err);
    fprintf(stderr, "  Compositor (drv=no, opfmt=rgba): %p err=%d\n", compositor, err);
    
    // Use pngenc to capture compositor output to file
    GF_Filter* png_encoder = gf_fs_load_filter(fs, "pngenc", &err);
    fprintf(stderr, "  PNG encoder: %p err=%d\n", png_encoder, err);
    
    // Link pngenc → output file
    GF_Filter* file_out = gf_fs_load_filter(fs, "fout:dst=/tmp/c2_api_composited.png", &err);
    fprintf(stderr, "  File output: %p err=%d\n", file_out, err);
    
    fprintf(stderr, "  Filter graph: scene.bt → btplay → compositor → pngenc → fout\n");
    
    // Phase 4: Run filter session
    fprintf(stderr, "\n--- Phase 4: Running filter session ---\n");
    auto t_start = std::chrono::steady_clock::now();
    
    // Run until the session completes (all streams done)
    // The compositor with fps=1 and image sources will render a few frames and stop
    GF_FilterEvent event;
    for (int iter = 0; iter < 5000; iter++) {
        gf_fs_run(fs);
        
        // Check if session is done
        // Check if we got a valid output by looking at the file
        FILE* check = fopen("/tmp/c2_api_composited.png", "r");
        if (check) {
            fseek(check, 0, SEEK_END);
            long sz = ftell(check);
            fclose(check);
            if (sz > 100) {
                fprintf(stderr, "  Session produced output after %d iterations (file=%ld bytes)\n", iter+1, sz);
                break;
            }
        }
    }
    
    auto t_end = std::chrono::steady_clock::now();
    double session_ms = std::chrono::duration<double, std::milli>(t_end - t_start).count();
    fprintf(stderr, "  Session time: %.2f ms\n", session_ms);
    
    // Phase 5: Read back and analyze composited output
    fprintf(stderr, "\n--- Phase 5: Analyze composited output ---\n");
    
    std::vector<uint8_t> output_frame;
    int out_w = 0, out_h = 0;
    if (!read_png_rgba("/tmp/c2_api_composited.png", output_frame, out_w, out_h)) {
        fprintf(stderr, "\n*** C2 FAIL: Could not read composited output PNG ***\n");
        gf_fs_del(fs);
        return 1;
    }
    
    fprintf(stderr, "  Output: %dx%d, %zu bytes\n", out_w, out_h, output_frame.size());
    fprintf(stderr, "  Output saved: /tmp/c2_api_composited.png\n");
    
    // Count pixel colors
    int green_px = 0, blue_px = 0, white_px = 0;
    for (int i = 0; i < out_w * out_h; i++) {
        uint8_t r = output_frame[i*4+0];
        uint8_t g = output_frame[i*4+1];
        uint8_t b = output_frame[i*4+2];
        uint8_t a = output_frame[i*4+3];
        if (g > 200 && r < 50 && b < 50 && a > 200) green_px++;
        if (b > 100 && r < 80 && g < 150 && a > 200) blue_px++;
        if (r > 200 && g > 200 && b > 200) white_px++;
    }
    
    fprintf(stderr, "  Green pixels (solid layer): %d\n", green_px);
    fprintf(stderr, "  Blue pixels (WPE bg): %d\n", blue_px);
    fprintf(stderr, "  White pixels (WPE text): %d\n", white_px);
    
    bool has_wpe = (blue_px > 100);
    bool has_solid = (green_px > 100);
    bool dims_ok = (out_w == W && out_h == H);
    
    fprintf(stderr, "  Dimensions match 1920x1080: %s\n", dims_ok ? "YES" : "NO");
    fprintf(stderr, "  WPE layer present: %s\n", has_wpe ? "YES" : "NO");
    fprintf(stderr, "  Solid layer present: %s\n", has_solid ? "YES" : "NO");
    
    // Metrics
    int64_t test_end_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(
        std::chrono::steady_clock::now().time_since_epoch()).count();
    double test_ms = (test_end_ns - test_start_ns) / 1e6;
    
    struct rusage ru;
    getrusage(RUSAGE_SELF, &ru);
    long rss_kb = ru.ru_maxrss;
    double cpu_sec = ru.ru_utime.tv_sec + ru.ru_utime.tv_usec / 1e6 +
                     ru.ru_stime.tv_sec + ru.ru_stime.tv_usec / 1e6;
    
    fprintf(stderr, "\n--- Performance Metrics ---\n");
    fprintf(stderr, "  Composition latency: %.2f ms\n", session_ms);
    fprintf(stderr, "  Total test time: %.2f ms\n", test_ms);
    fprintf(stderr, "  CPU time: %.3f sec\n", cpu_sec);
    fprintf(stderr, "  Peak RSS: %ld KB (%.1f MB)\n", rss_kb, rss_kb / 1024.0);
    
    // Verdict
    fprintf(stderr, "\n========================================\n");
    if (has_wpe && has_solid) {
        fprintf(stderr, " C2 PASS — GPAC performed real composition\n");
        fprintf(stderr, "========================================\n\n");
        fprintf(stderr, " Filter graph:\n");
        fprintf(stderr, "   scene.bt (BIFS) ── btplay ──┐\n");
        fprintf(stderr, "   PNG(wpe) ──┐                 ├── compositor (drv=no, opfmt=rgba) ── pngenc ── fout\n");
        fprintf(stderr, "   PNG(solid) ─┘  (file:// refs)\n\n");
        fprintf(stderr, " Proof of GPAC composition:\n");
        fprintf(stderr, "   • BIFS scene describes layer arrangement via OrderedGroup/Layer2D/Transform2D\n");
        fprintf(stderr, "   • Background2D nodes reference PNG files via file:// URLs\n");
        fprintf(stderr, "   • GPAC compositor (CPU 2D, drv=no) rasterizes both layers\n");
        fprintf(stderr, "   • No manual C++ pixel blending in this test\n");
        fprintf(stderr, "   • WPE layer (blue bg): %d pixels\n", blue_px);
        fprintf(stderr, "   • Solid layer (green rect): %d pixels\n", green_px);
        fprintf(stderr, "   • Both layers visible in compositor output: YES\n");
        fprintf(stderr, "   • Output pixel format: RGBA\n");
        fprintf(stderr, "   • Output dimensions: %dx%d\n", out_w, out_h);
    } else {
        fprintf(stderr, " C2 FAIL\n");
        fprintf(stderr, "========================================\n");
        fprintf(stderr, " Missing: WPE=%s Solid=%s\n",
                has_wpe ? "present" : "MISSING", has_solid ? "present" : "MISSING");
    }
    
    gf_fs_del(fs);
    return (has_wpe && has_solid) ? 0 : 1;
}
