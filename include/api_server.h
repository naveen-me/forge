#ifndef TARVA_API_SERVER_H
#define TARVA_API_SERVER_H

#include <string>
#include <memory>
#include <thread>
#include <atomic>
#include "httplib.h"
#include "scene_controller.h"
#include "runtime_stats.h"

namespace tarva {

class ApiServer {
public:
    ApiServer(int port, std::shared_ptr<SceneController> controller,
              std::shared_ptr<RuntimeStats> stats = nullptr);
    ~ApiServer();

    bool start();
    void stop();

private:
    int port_;
    std::shared_ptr<SceneController> controller_;
    std::shared_ptr<RuntimeStats> stats_;
    httplib::Server server_;
    std::thread server_thread_;
    std::atomic<bool> running_{false};

    void setup_routes();
};

} // namespace tarva

#endif // TARVA_API_SERVER_H
