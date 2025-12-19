using System;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class PlayoutArbiter
    {
        private readonly ISchedulerService _scheduler;
        private readonly IBufferManager _bufferManager;
        private readonly LogoService _logoService;

        public PlayoutArbiter(ISchedulerService scheduler, IBufferManager bufferManager, LogoService logoService)
        {
            _scheduler = scheduler;
            _bufferManager = bufferManager;
            _logoService = logoService;
        }

        public PlayoutDecision Decide(DateTime now)
        {
            var scheduled = _scheduler.GetActiveItem(now);
            if (scheduled != null)
                return PlayoutDecision.FromSchedule(scheduled);

            var bufferItem = _bufferManager.GetNextBufferItem();
            if (bufferItem != null)
                return PlayoutDecision.FromBuffer(bufferItem);

            return PlayoutDecision.ShowLogo();
        }

        // Method to get decision without advancing buffer
        public PlayoutDecision PeekDecision(DateTime now)
        {
            var scheduled = _scheduler.GetActiveItem(now);
            if (scheduled != null)
                return PlayoutDecision.FromSchedule(scheduled);

            var bufferItem = _bufferManager.GetCurrentBufferItem();
            if (bufferItem != null)
                return PlayoutDecision.FromBuffer(bufferItem);

            return PlayoutDecision.ShowLogo();
        }
    }
}