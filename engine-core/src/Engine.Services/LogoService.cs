using System;
using Microsoft.Extensions.Configuration;
using PlayoutEngine.Models;

namespace PlayoutEngine.Services
{
    public class LogoService
    {
        private readonly string _logoPath;

        public LogoService(IConfiguration configuration)
        {
            _logoPath = configuration.GetValue<string>("Logo:Path") ?? "media-assets/logo.png";
        }

        public ExpectedState GetLogoState()
        {
            return ExpectedState.CreateOverlayOnly("GLOBAL_LOGO");
        }

        public string GetLogoPath()
        {
            return _logoPath;
        }
    }
}