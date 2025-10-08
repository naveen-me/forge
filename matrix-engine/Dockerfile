# Use Ubuntu 22.04 as the base image
FROM ubuntu:22.04

# Avoid interactive prompts during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install necessary dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    software-properties-common \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Add OBS PPA and install OBS Studio
RUN add-apt-repository ppa:obsproject/obs-studio && \
    apt-get update && \
    apt-get install -y --no-install-recommends obs-studio

# Download and install obs-websocket plugin
RUN wget https://github.com/obsproject/obs-websocket/releases/download/5.4.2/obs-websocket-5.4.2-linux-x86_64.zip -O /tmp/obs-websocket.zip && \
    unzip /tmp/obs-websocket.zip -d /tmp/obs-websocket-unzipped && \
    mkdir -p /usr/lib/x86_64-linux-gnu/obs-plugins/ && \
    cp -r /tmp/obs-websocket-unzipped/obs-plugins/64bit/* /usr/lib/x86_64-linux-gnu/obs-plugins/ && \
    rm -rf /tmp/obs-websocket.zip /tmp/obs-websocket-unzipped

# Expose the default OBS WebSocket port
EXPOSE 4455

# Set up a default command (can be overridden)
CMD ["obs", "--start-web-socket"]
