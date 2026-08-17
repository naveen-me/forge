#include "bounded_queue.h"
#include "logger.h"
#include <cassert>
#include <iostream>
#include <thread>
#include <atomic>
#include <vector>

void test_capacity_bound() {
    tarva::BoundedQueue<int> q(4);

    assert(q.capacity() == 4);
    assert(q.push(1));
    assert(q.push(2));
    assert(q.push(3));
    assert(q.push(4));
    assert(q.size() == 4);

    // The 5th push must block until space frees up (verified in backpressure test);
    // here we verify the queue never exceeds capacity while a consumer drains it.
    std::atomic<bool> stop{false};
    std::atomic<size_t> max_seen{0};

    std::thread consumer([&]() {
        int item = 0;
        while (!stop || q.size() > 0) {
            if (q.pop(item)) {
                size_t s = q.size();
                size_t cur = max_seen.load();
                while (cur < s && !max_seen.compare_exchange_weak(cur, s)) {}
            }
        }
    });

    for (int i = 0; i < 10000; ++i) {
        assert(q.push(i));
    }
    stop = true;
    q.close();
    consumer.join();

    // A strictly bounded queue must never have observed more than capacity items
    assert(max_seen.load() <= 4);
    LOG_INFO("capacity bound test passed (max observed queue size = " + std::to_string(max_seen.load()) + ")");
}

void test_backpressure_blocks_producer() {
    tarva::BoundedQueue<int> q(1);

    assert(q.push(1));
    // Queue is full; a second push should block until a consumer pops.
    std::atomic<bool> pushed{false};
    std::thread producer([&]() {
        q.push(2);
        pushed = true;
    });

    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    assert(!pushed.load()); // producer is blocked: backpressure is working

    int item = 0;
    assert(q.pop(item));
    assert(item == 1);

    producer.join();
    assert(pushed.load());

    assert(q.pop(item));
    assert(item == 2);
    LOG_INFO("backpressure test passed");
}

void test_close_drains_then_fails() {
    tarva::BoundedQueue<int> q(8);
    for (int i = 0; i < 5; ++i) q.push(i);

    q.close();

    int item = -1;
    for (int i = 0; i < 5; ++i) {
        assert(q.pop(item));
        assert(item == i);
    }
    // Drained: pop must now report closed
    assert(!q.pop(item));
    assert(!q.push(99));
    LOG_INFO("close/drain test passed");
}

void test_producer_consumer_no_loss() {
    constexpr int N = 100000;
    tarva::BoundedQueue<int> q(16);

    std::atomic<int> consumed{0};
    std::thread consumer([&]() {
        int item = 0;
        while (q.pop(item)) {
            assert(item == consumed.load());
            consumed.fetch_add(1);
        }
    });

    std::thread producer([&]() {
        for (int i = 0; i < N; ++i) {
            assert(q.push(i));
        }
        q.close();
    });

    producer.join();
    consumer.join();

    assert(consumed.load() == N);
    LOG_INFO("producer/consumer no-loss test passed (" + std::to_string(N) + " items)");
}

int main() {
    tarva::Logger::instance().set_level(tarva::LogLevel::DEBUG);
    LOG_INFO("Testing BoundedQueue...");

    test_capacity_bound();
    test_backpressure_blocks_producer();
    test_close_drains_then_fails();
    test_producer_consumer_no_loss();

    LOG_INFO("All BoundedQueue tests passed successfully!");
    return 0;
}
