#ifndef TARVA_SOURCE_MANAGER_H
#define TARVA_SOURCE_MANAGER_H

#include <string>
#include <map>
#include <memory>
#include <mutex>
#include <vector>
#include "media_sources.h"
#include "scene_schema.h"

namespace tarva {

enum class SourceState {
    DECLARED,
    PREPARING,
    READY,
    ACTIVE,
    ERROR,
    ENDED
};

struct SourceHandle {
    std::string id;
    std::string uri;
    std::string type;
    SourceState state = SourceState::DECLARED;
    std::string error_message;
    std::shared_ptr<MediaSource> media_source;
};

class SourceManager {
public:
    SourceManager();

    // Protocol capability check
    bool supports_protocol(const std::string& uri) const;

    // Source preparation & preloading
    std::shared_ptr<SourceHandle> prepare_source(const Layer& layer, int canvas_w, int canvas_h);

    // Get or prepare active handle
    std::shared_ptr<SourceHandle> get_source(const std::string& layer_id) const;

    // Remove source handle
    void remove_source(const std::string& layer_id);

    // List all managed source states
    std::vector<SourceHandle> list_sources() const;

private:
    mutable std::mutex manager_mutex_;
    std::map<std::string, std::shared_ptr<SourceHandle>> sources_;
};

} // namespace tarva

#endif // TARVA_SOURCE_MANAGER_H
