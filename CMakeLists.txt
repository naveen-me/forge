cmake_minimum_required(VERSION 3.16)
project(tarva_playout VERSION 0.1.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(PkgConfig REQUIRED)
find_package(Threads REQUIRED)

pkg_check_modules(GPAC REQUIRED gpac)
pkg_check_modules(AVCODEC REQUIRED libavcodec)
pkg_check_modules(AVFORMAT REQUIRED libavformat)
pkg_check_modules(SWSCALE REQUIRED libswscale)
pkg_check_modules(SWRESAMPLE REQUIRED libswresample)
pkg_check_modules(AVUTIL REQUIRED libavutil)
pkg_check_modules(CAIRO REQUIRED cairo)
pkg_check_modules(WEBKIT REQUIRED webkit2gtk-4.1)
pkg_check_modules(WPE REQUIRED wpe-1.0)
pkg_check_modules(WPE_FDO REQUIRED wpebackend-fdo-1.0)

find_package(nlohmann_json REQUIRED)

include_directories(
    include
    ${GPAC_INCLUDE_DIRS}
    ${AVCODEC_INCLUDE_DIRS}
    ${AVFORMAT_INCLUDE_DIRS}
    ${SWSCALE_INCLUDE_DIRS}
    ${SWRESAMPLE_INCLUDE_DIRS}
    ${AVUTIL_INCLUDE_DIRS}
    ${CAIRO_INCLUDE_DIRS}
    ${WEBKIT_INCLUDE_DIRS}
    ${WPE_INCLUDE_DIRS}
    ${WPE_FDO_INCLUDE_DIRS}
)

link_directories(
    ${GPAC_LIBRARY_DIRS}
    ${AVCODEC_LIBRARY_DIRS}
    ${AVFORMAT_LIBRARY_DIRS}
    ${SWSCALE_LIBRARY_DIRS}
    ${AVUTIL_LIBRARY_DIRS}
    ${CAIRO_LIBRARY_DIRS}
    ${WEBKIT_LIBRARY_DIRS}
    ${WPE_LIBRARY_DIRS}
    ${WPE_FDO_LIBRARY_DIRS}
)

# Core engine library
add_library(tarva_core
    src/logger.cpp
    src/scene_schema.cpp
    src/wpe_html_renderer.cpp
    src/media_sources.cpp
    src/gpac_compositor.cpp
    src/media_output.cpp
    src/benchmark_harness.cpp
    src/timeline_engine.cpp
    src/source_manager.cpp
    src/scene_controller.cpp
    src/api_server.cpp
    src/audio_mixer.cpp
)

target_link_libraries(tarva_core
    PUBLIC
    ${GPAC_LIBRARIES}
    ${AVCODEC_LIBRARIES}
    ${AVFORMAT_LIBRARIES}
    ${SWSCALE_LIBRARIES}
    ${SWRESAMPLE_LIBRARIES}
    ${AVUTIL_LIBRARIES}
    ${CAIRO_LIBRARIES}
    ${WEBKIT_LIBRARIES}
    ${WPE_LIBRARIES}
    ${WPE_FDO_LIBRARIES}
    nlohmann_json::nlohmann_json
    Threads::Threads
)

# Main server executable
add_executable(tarva_playout src/main.cpp)
target_link_libraries(tarva_playout PRIVATE tarva_core)

enable_testing()

add_executable(test_schema tests/test_schema.cpp)
target_link_libraries(test_schema PRIVATE tarva_core)
add_test(NAME test_schema COMMAND $<TARGET_FILE:test_schema>)

add_executable(test_wpe tests/test_wpe.cpp)
target_link_libraries(test_wpe PRIVATE tarva_core)
add_test(NAME test_wpe COMMAND xvfb-run -a $<TARGET_FILE:test_wpe>)
set_tests_properties(test_wpe PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(test_sources tests/test_sources.cpp)
target_link_libraries(test_sources PRIVATE tarva_core)
add_test(NAME test_sources COMMAND xvfb-run -a $<TARGET_FILE:test_sources>)
set_tests_properties(test_sources PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(test_compositor tests/test_compositor.cpp)
target_link_libraries(test_compositor PRIVATE tarva_core)
add_test(NAME test_compositor COMMAND xvfb-run -a $<TARGET_FILE:test_compositor>)
set_tests_properties(test_compositor PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(test_output tests/test_output.cpp)
target_link_libraries(test_output PRIVATE tarva_core)
add_test(NAME test_output COMMAND xvfb-run -a $<TARGET_FILE:test_output>)
set_tests_properties(test_output PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(test_timeline tests/test_timeline.cpp)
target_link_libraries(test_timeline PRIVATE tarva_core)
add_test(NAME test_timeline COMMAND $<TARGET_FILE:test_timeline>)

add_executable(test_source_manager tests/test_source_manager.cpp)
target_link_libraries(test_source_manager PRIVATE tarva_core)
add_test(NAME test_source_manager COMMAND xvfb-run -a $<TARGET_FILE:test_source_manager>)
set_tests_properties(test_source_manager PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(test_api tests/test_api.cpp)
target_link_libraries(test_api PRIVATE tarva_core)
add_test(NAME test_api COMMAND $<TARGET_FILE:test_api>)

add_executable(test_effects tests/test_effects.cpp)
target_link_libraries(test_effects PRIVATE tarva_core)
add_test(NAME test_effects COMMAND xvfb-run -a $<TARGET_FILE:test_effects>)
set_tests_properties(test_effects PROPERTIES ENVIRONMENT "WEBKIT_DISABLE_COMPOSITING_MODE=1;WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1;NO_AT_BRIDGE=1")

add_executable(run_poc_benchmark benchmarks/run_poc_benchmark.cpp)
target_link_libraries(run_poc_benchmark PRIVATE tarva_core)
