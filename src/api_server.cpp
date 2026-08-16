#include "api_server.h"
#include "logger.h"
#include <nlohmann/json.hpp>

namespace tarva {

ApiServer::ApiServer(int port, std::shared_ptr<SceneController> controller)
    : port_(port), controller_(controller) {}

ApiServer::~ApiServer() {
    stop();
}

void ApiServer::setup_routes() {
    // GET /health
    server_.Get("/health", [](const httplib::Request&, httplib::Response& res) {
        nlohmann::json j = { {"status", "ok"}, {"engine", "TARVA Playout"} };
        res.set_content(j.dump(), "application/json");
    });

    // GET /status
    server_.Get("/status", [this](const httplib::Request&, httplib::Response& res) {
        Scene scene = controller_->current_scene();
        nlohmann::json j = {
            {"status", "running"},
            {"canvas", scene.canvas},
            {"revision", scene.revision},
            {"layerCount", scene.layers.size()}
        };
        res.set_content(j.dump(), "application/json");
    });

    // PUT /scene
    server_.Put("/scene", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            Scene scene = j.get<Scene>();
            controller_->update_full_scene(scene);
            res.set_content(R"({"status":"success","message":"Scene replaced"})", "application/json");
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /layers
    server_.Post("/layers", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            Layer layer = j.get<Layer>();
            controller_->add_layer(layer);
            res.set_content(R"({"status":"success","message":"Layer added"})", "application/json");
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /layers/patch
    server_.Post("/layers/patch", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json patch = nlohmann::json::parse(req.body);
            std::string layer_id = patch.value("id", "");
            if (!layer_id.empty() && controller_->patch_layer(layer_id, patch)) {
                res.set_content(R"({"status":"success","message":"Layer patched"})", "application/json");
            } else {
                res.status = 44;
                res.set_content(R"({"error":"Layer not found"})", "application/json");
            }
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /layers/delete
    server_.Post("/layers/delete", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            std::string layer_id = j.value("id", "");
            if (!layer_id.empty() && controller_->delete_layer(layer_id)) {
                res.set_content(R"({"status":"success","message":"Layer deleted"})", "application/json");
            } else {
                res.status = 404;
                res.set_content(R"({"error":"Layer not found"})", "application/json");
            }
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /layers/hide
    server_.Post("/layers/hide", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            std::string layer_id = j.value("id", "");
            if (!layer_id.empty() && controller_->set_layer_hidden(layer_id, true)) {
                res.set_content(R"({"status":"success","message":"Layer hidden"})", "application/json");
            } else {
                res.status = 404;
                res.set_content(R"({"error":"Layer not found"})", "application/json");
            }
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /layers/show
    server_.Post("/layers/show", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            std::string layer_id = j.value("id", "");
            if (!layer_id.empty() && controller_->set_layer_hidden(layer_id, false)) {
                res.set_content(R"({"status":"success","message":"Layer shown"})", "application/json");
            } else {
                res.status = 404;
                res.set_content(R"({"error":"Layer not found"})", "application/json");
            }
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });

    // POST /schedule
    server_.Post("/schedule", [this](const httplib::Request& req, httplib::Response& res) {
        try {
            nlohmann::json j = nlohmann::json::parse(req.body);
            std::string execute_at_str = j.value("executeAt", "00:00:00");
            int64_t execute_at_ns = parse_time_str(execute_at_str);
            std::string op_type = j.value("type", "patch_layer");
            std::string layer_id = j.value("id", "");
            nlohmann::json payload = j.value("patch", nlohmann::json::object());

            controller_->schedule_operation(execute_at_ns, op_type, layer_id, payload);
            res.set_content(R"({"status":"success","message":"Operation scheduled"})", "application/json");
        } catch (const std::exception& ex) {
            res.status = 400;
            res.set_content(nlohmann::json({{"error", ex.what()}}).dump(), "application/json");
        }
    });
}

bool ApiServer::start() {
    setup_routes();
    running_ = true;
    server_thread_ = std::thread([this]() {
        LOG_INFO("ApiServer listening on port " + std::to_string(port_));
        server_.listen("0.0.0.0", port_);
    });

    int attempts = 0;
    while (!server_.is_running() && attempts < 100) {
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
        attempts++;
    }
    return server_.is_running();
}

void ApiServer::stop() {
    if (running_) {
        running_ = false;
        server_.stop();
        if (server_thread_.joinable()) {
            server_thread_.join();
        }
        LOG_INFO("ApiServer stopped.");
    }
}

} // namespace tarva
