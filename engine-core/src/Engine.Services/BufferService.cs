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

        public void AddItem(BufferItem item)
        {
            _items.Add(item);
        }

        public void Clear()
        {
            _items.Clear();
            _currentIndex = 0;
        }

        public BufferItem? GetNext()
        {
            if (_items.Count == 0)
                return null;

            var item = _items[_currentIndex];
            _currentIndex = (_currentIndex + 1) % _items.Count;
            return item;
        }
    }
}