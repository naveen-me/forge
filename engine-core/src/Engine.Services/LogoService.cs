using System;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class LogoService
    {
        private readonly string _logoPath;

        public LogoService(string logoPath = "")
        {
            _logoPath = logoPath;
        }

        public ExpectedState GetLogoState()
        {
            return ExpectedState.CreateOverlayOnly("GLOBAL_LOGO");
        }
    }
}