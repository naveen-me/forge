#include "source_manager.h"
#include "logger.h"
#include <algorithm>

namespace tarva {

SourceManager::SourceManager() {}

bool SourceManager::supports_protocol(const std::string& uri) const {
    if (uri.empty()) return true; // Text or dummy sources
    if (uri.rfind("http://", 0) == 0 || uri.rfind("https://", 0) == 0) return true;
    if (uri.rfind("srt://", 0) == 0) return true;
    if (uri.rfind("rtmp://", 0) == 0 || uri.rfind("rtsp://", 0) == 0) return true;
    if (uri[0] == '/' || uri.rfind("./", 0) == 0 || uri.rfind("../", 0) == 0) return true;
    return false;
}

std::shared_ptr<SourceHandle> SourceManager::prepare_source(const Layer& layer, int canvas_w, int canvas_h) {
    std::lock_guard<std::mutex> lock(manager_mutex_);

    auto it = sources_.find(layer.id);
    if (it != sources_.end() && it->second->state != SourceState::ERROR) {
        return it->second;
    }

    auto handle = std::make_shared<SourceHandle>();
    handle->id = layer.id;
    handle->uri = layer.source;
    handle->type = layer.type;
    handle->state = SourceState::PREPARING;

    if (!supports_protocol(layer.source)) {
        handle->state = SourceState::ERROR;
        handle->error_message = "Unsupported protocol or URL scheme: " + layer.source;
        LOG_ERROR("SourceManager: " + handle->error_message);
        sources_[layer.id] = handle;
        return handle;
    }

    try {
        if (layer.type == "video") {
            auto v_src = std::make_shared<VideoSource>();
            if (v_src->load(layer.source, layer.width, layer.height)) {
                handle->media_source = v_src;
                handle->state = SourceState::READY;
            } else {
                handle->state = SourceState::ERROR;
                handle->error_message = "Failed to load video source: " + layer.source;
            }
        } else if (layer.type == "image") {
            auto img_src = std::make_shared<ImageSource>();
            if (img_src->load(layer.source, layer.width, layer.height)) {
                handle->media_source = img_src;
                handle->state = SourceState::READY;
            } else {
                handle->state = SourceState::ERROR;
                handle->error_message = "Failed to load image source: " + layer.source;
            }
        } else if (layer.type == "html") {
            auto html_src = std::make_shared<HtmlSource>();
            handle->media_source = html_src;
            handle->state = SourceState::READY;
        } else if (layer.type == "text") {
            auto txt_src = std::make_shared<TextSource>(layer.text, layer.font_size, layer.font_color, layer.background_color);
            txt_src->load("", layer.width, layer.height);
            handle->media_source = txt_src;
            handle->state = SourceState::READY;
        } else {
            handle->state = SourceState::ERROR;
            handle->error_message = "Unknown layer type: " + layer.type;
        }
    } catch (const std::exception& ex) {
        handle->state = SourceState::ERROR;
        handle->error_message = std::string("Exception loading source: ") + ex.what();
        LOG_ERROR("SourceManager caught exception: " + handle->error_message);
    }

    sources_[layer.id] = handle;
    return handle;
}

std::shared_ptr<SourceHandle> SourceManager::get_source(const std::string& layer_id) const {
    std::lock_guard<std::mutex> lock(manager_mutex_);
    auto it = sources_.find(layer_id);
    if (it != sources_.end()) {
        return it->second;
    }
    return nullptr;
}

void SourceManager::remove_source(const std::string& layer_id) {
    std::lock_guard<std::mutex> lock(manager_mutex_);
    sources_.erase(layer_id);
}

std::vector<SourceHandle> SourceManager::list_sources() const {
    std::lock_guard<std::mutex> lock(manager_mutex_);
    std::vector<SourceHandle> result;
    for (const auto& [id, handle] : sources_) {
        if (handle) {
            result.push_back(*handle);
        }
    }
    return result;
}

} // namespace tarva
