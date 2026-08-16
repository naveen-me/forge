# Multi-stage Dockerfile for TARVA Headless Playout Engine (Linux VPS / CPU-first)

# Stage 1: Build stage
FROM ubuntu:24.04 AS builder

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    meson \
    ninja-build \
    pkg-config \
    git \
    curl \
    libgpac-dev \
    gpac \
    ffmpeg \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    libswresample-dev \
    libavutil-dev \
    libcairo2-dev \
    libwebkit2gtk-4.1-dev \
    libglib2.0-dev \
    libepoxy-dev \
    libxkbcommon-dev \
    libwayland-dev \
    wayland-protocols \
    libegl1-mesa-dev \
    libgles2-mesa-dev \
    libgbm-dev \
    nlohmann-json3-dev \
    && rm -rf /var/lib/apt/lists/*

# Build libwpe and WPEBackend-fdo
RUN cd /tmp && git clone --depth 1 https://github.com/WebPlatformForEmbedded/libwpe.git && \
    cd libwpe && meson setup build && ninja -C build && ninja -C build install

RUN cd /tmp && git clone --depth 1 https://github.com/Igalia/WPEBackend-fdo.git && \
    cd WPEBackend-fdo && meson setup build && ninja -C build && ninja -C build install

RUN ldconfig

WORKDIR /app
COPY . /app

RUN mkdir -p build && cd build && cmake .. && make -j$(nproc) tarva_playout

# Stage 2: Production Runtime stage
FROM ubuntu:24.04 AS runtime

ENV DEBIAN_FRONTEND=noninteractive
ENV WEBKIT_DISABLE_COMPOSITING_MODE=1
ENV WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1
ENV NO_AT_BRIDGE=1
ENV PLAYOUT_CONFIG=/etc/playout/config.json
ENV PLAYOUT_LOG_LEVEL=info

RUN apt-get update && apt-get install -y \
    ca-certificates \
    xvfb \
    ffmpeg \
    gpac \
    libgpac12t64 \
    libavcodec60 \
    libavformat60 \
    libswscale7 \
    libswresample4 \
    libavutil58 \
    libcairo2 \
    libwebkit2gtk-4.1-0 \
    libglib2.0-0t64 \
    libepoxy0 \
    libxkbcommon0 \
    libwayland-client0 \
    libwayland-server0 \
    libwayland-egl1 \
    libegl1-mesa \
    libgles2-mesa \
    libgbm1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy compiled shared libraries from builder
COPY --from=builder /usr/local/lib/x86_64-linux-gnu/ /usr/local/lib/x86_64-linux-gnu/
RUN ldconfig

WORKDIR /app
COPY --from=builder /app/build/tarva_playout /app/tarva_playout
COPY example_scenes /app/example_scenes

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["xvfb-run", "-a", "/app/tarva_playout"]
