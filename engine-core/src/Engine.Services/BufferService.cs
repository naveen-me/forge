using System;
using System.Collections.Generic;
using System.Linq;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class BufferService
    {
        private readonly List<BufferItem> _items = new List<BufferItem>();
        private int _currentIndex = 0;
        private readonly object _lock = new object();

        public void AddItem(BufferItem item)
        {
            lock (_lock)
            {
                _items.Add(item);
            }
        }

        public void AddItems(IEnumerable<BufferItem> items)
        {
            lock (_lock)
            {
                _items.AddRange(items);
            }
        }

        public void Clear()
        {
            lock (_lock)
            {
                _items.Clear();
                _currentIndex = 0;
            }
        }

        public BufferItem? GetNext()
        {
            lock (_lock)
            {
                if (_items.Count == 0)
                    return null;

                var item = _items[_currentIndex];
                _currentIndex = (_currentIndex + 1) % _items.Count;
                return item;
            }
        }

        public BufferItem? GetCurrent()
        {
            lock (_lock)
            {
                if (_items.Count == 0 || _currentIndex >= _items.Count)
                    return null;

                return _items[_currentIndex];
            }
        }

        public void Reset()
        {
            lock (_lock)
            {
                _currentIndex = 0;
            }
        }

        public int Count => _items.Count;
    }
}