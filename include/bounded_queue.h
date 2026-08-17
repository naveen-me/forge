#ifndef TARVA_BOUNDED_QUEUE_H
#define TARVA_BOUNDED_QUEUE_H

#include <queue>
#include <mutex>
#include <condition_variable>
#include <cstddef>

namespace tarva {

// Thread-safe FIFO queue with a hard capacity bound.
//
// push() blocks (backpressure) while the queue is full so a fast producer can
// never grow memory without limit; pop() blocks while the queue is empty.
// close() wakes all blocked callers; push()/pop() return false once closed
// (pop drains remaining items first).
//
// This is the canonical primitive for every producer/consumer boundary in the
// engine (e.g. composite -> encode) — unbounded std::queue growth is a
// documented audit finding (A5) and must not be reintroduced.
template <typename T>
class BoundedQueue {
public:
    explicit BoundedQueue(size_t capacity) : capacity_(capacity > 0 ? capacity : 1) {}

    // Blocks while full. Returns false if the queue is closed (item not enqueued).
    bool push(T item) {
        std::unique_lock<std::mutex> lock(mutex_);
        not_full_.wait(lock, [this]() { return closed_ || q_.size() < capacity_; });
        if (closed_) return false;
        q_.push(std::move(item));
        not_empty_.notify_one();
        return true;
    }

    // Blocks while empty. Returns false when closed and drained.
    bool pop(T& item) {
        std::unique_lock<std::mutex> lock(mutex_);
        not_empty_.wait(lock, [this]() { return closed_ || !q_.empty(); });
        if (q_.empty()) return false; // closed and drained
        item = std::move(q_.front());
        q_.pop();
        not_full_.notify_one();
        return true;
    }

    // Non-blocking pop; returns false if empty or closed.
    bool try_pop(T& item) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (q_.empty()) return false;
        item = std::move(q_.front());
        q_.pop();
        not_full_.notify_one();
        return true;
    }

    size_t size() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return q_.size();
    }

    size_t capacity() const { return capacity_; }

    bool is_closed() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return closed_;
    }

    void close() {
        std::lock_guard<std::mutex> lock(mutex_);
        closed_ = true;
        not_full_.notify_all();
        not_empty_.notify_all();
    }

private:
    mutable std::mutex mutex_;
    std::condition_variable not_full_;
    std::condition_variable not_empty_;
    std::queue<T> q_;
    size_t capacity_;
    bool closed_ = false;
};

} // namespace tarva

#endif // TARVA_BOUNDED_QUEUE_H
