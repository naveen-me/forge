-- OBS Event Bridge Script
-- This script logs important events that the playout engine can detect via WebSocket

obs = require("obslua")

-- Configuration
local scene_change_callback_registered = false
local source_callback_registered = false

-- Function to log event (the playout engine will detect these via OBS WebSocket)
function log_event(event_type, data)
    local log_msg = "OBS_EVENT: " .. event_type
    if data and data.source_name then
        log_msg = log_msg .. " - Source: " .. data.source_name
    elseif data and data.scene_name then
        log_msg = log_msg .. " - Scene: " .. data.scene_name
    end
    obs.script_log(obs.LOG_INFO, log_msg)
end

-- Callback functions for different OBS events
function on_scene_change()
    local current_scene = obs.obs_frontend_get_current_scene()
    if current_scene then
        local scene_name = obs.obs_source_get_name(current_scene)
        log_event("SCENE_CHANGED", { scene_name = scene_name })
        obs.obs_source_release(current_scene)
    end
end

function on_source_show(ptr)
    local source_name = obs.obs_source_get_name(ptr)
    log_event("SOURCE_SHOWN", { source_name = source_name })
end

function on_source_hide(ptr)
    local source_name = obs.obs_source_get_name(ptr)
    log_event("SOURCE_HIDDEN", { source_name = source_name })
end

function on_media_playing(ptr)
    local source_name = obs.obs_source_get_name(ptr)
    log_event("MEDIA_PLAYING", { source_name = source_name })
end

function on_media_stopped(ptr)
    local source_name = obs.obs_source_get_name(ptr)
    log_event("MEDIA_STOPPED", { source_name = source_name })
end

function on_media_ended(ptr)
    local source_name = obs.obs_source_get_name(ptr)
    log_event("MEDIA_ENDED", { source_name = source_name })
end

-- Register callbacks
function script_update(settings)
    -- Register scene change callback if not already registered
    if not scene_change_callback_registered then
        obs.obs_frontend_add_current_scene_callback(on_scene_change)
        scene_change_callback_registered = true
        obs.script_log(obs.LOG_INFO, "Scene change callback registered")
    end

    -- Register source callbacks if not already registered
    if not source_callback_registered then
        -- Register signal handlers for source visibility
        local sh = obs.obs_get_signal_handler()
        obs.signal_handler_connect(sh, "source_show", on_source_show)
        obs.signal_handler_connect(sh, "source_hide", on_source_hide)
        obs.signal_handler_connect(sh, "media_playing", on_media_playing)
        obs.signal_handler_connect(sh, "media_stopped", on_media_stopped)
        obs.signal_handler_connect(sh, "media_ended", on_media_ended)
        source_callback_registered = true
        obs.script_log(obs.LOG_INFO, "Source event callbacks registered")
    end
end

-- Script description
function script_description()
    return "OBS Event Bridge - Logs events that the playout engine monitors via OBS WebSocket"
end

-- Script startup
function script_load(settings)
    obs.script_log(obs.LOG_INFO, "OBS Event Bridge loaded")

    -- Register frontend events
    obs.obs_frontend_add_event_callback(function(event)
        if event == obs.OBS_FRONTEND_EVENT_SCENE_CHANGED then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: FRONTEND_SCENE_CHANGED")
        elseif event == obs.OBS_FRONTEND_EVENT_SOURCE_SHOW then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: FRONTEND_SOURCE_SHOW")
        elseif event == obs.OBS_FRONTEND_EVENT_SOURCE_HIDE then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: FRONTEND_SOURCE_HIDE")
        elseif event == obs.OBS_FRONTEND_EVENT_RECORDING_STARTED then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: RECORDING_STARTED")
        elseif event == obs.OBS_FRONTEND_EVENT_RECORDING_STOPPED then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: RECORDING_STOPPED")
        elseif event == obs.OBS_FRONTEND_EVENT_STREAMING_STARTED then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: STREAMING_STARTED")
        elseif event == obs.OBS_FRONTEND_EVENT_STREAMING_STOPPED then
            obs.script_log(obs.LOG_INFO, "OBS_EVENT: STREAMING_STOPPED")
        end
    end)
end

-- Script shutdown
function script_unload()
    obs.script_log(obs.LOG_INFO, "OBS Event Bridge unloaded")

    if scene_change_callback_registered then
        obs.obs_frontend_remove_current_scene_callback(on_scene_change)
        scene_change_callback_registered = false
    end

    if source_callback_registered then
        local sh = obs.obs_get_signal_handler()
        obs.signal_handler_disconnect(sh, "source_show", on_source_show)
        obs.signal_handler_disconnect(sh, "source_hide", on_source_hide)
        obs.signal_handler_disconnect(sh, "media_playing", on_media_playing)
        obs.signal_handler_disconnect(sh, "media_stopped", on_media_stopped)
        obs.signal_handler_disconnect(sh, "media_ended", on_media_ended)
        source_callback_registered = false
    end
end

-- Script properties (for UI configuration if needed)
function script_properties()
    local props = obs.obs_properties_create()
    return props
end