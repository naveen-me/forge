using System;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class PlayoutArbiter
    {
        private readonly ISchedulerService _scheduler;
        private readonly BufferService _buffer;

        public PlayoutArbiter(ISchedulerService scheduler, BufferService buffer)
        {
            _scheduler = scheduler;
            _buffer = buffer;
        }

        public PlayoutDecision Decide(DateTime now)
        {
            var scheduled = _scheduler.GetActiveItem(now);
            if (scheduled != null)
                return PlayoutDecision.FromSchedule(scheduled);

            var bufferItem = _buffer.GetNext();
            if (bufferItem != null)
                return PlayoutDecision.FromBuffer(bufferItem);

            return PlayoutDecision.ShowLogo();
        }
    }
}