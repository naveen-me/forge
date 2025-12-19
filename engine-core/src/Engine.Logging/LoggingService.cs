using Microsoft.Extensions.Logging;
using Serilog;
using Serilog.Core;
using System;
using MELILogger = Microsoft.Extensions.Logging.ILogger;

namespace PlayoutEngine.Logging
{
    public interface ILoggingService
    {
        MELILogger CreateLogger(string categoryName);
        void ConfigureLogging(string configPath);
    }

    public class LoggingService : ILoggingService
    {
        private LoggingLevelSwitch _levelSwitch;

        public LoggingService()
        {
            _levelSwitch = new LoggingLevelSwitch();
        }

        public MELILogger CreateLogger(string categoryName)
        {
            return LoggerFactory.Create(builder =>
            {
                builder.AddSerilog(CreateSerilogLogger());
            }).CreateLogger(categoryName);
        }

        public void ConfigureLogging(string configPath = "")
        {
            // In a real implementation, this would load from the config file
            var loggerConfig = new LoggerConfiguration()
                .MinimumLevel.ControlledBy(_levelSwitch)
                .WriteTo.Console(outputTemplate: 
                    "{Timestamp:yyyy-MM-dd HH:mm:ss.fff zzz} [{Level:u3}] ({SourceContext}) {Message:lj}{NewLine}{Exception}")
                .WriteTo.File("./logs/engine.log", 
                    rollingInterval: RollingInterval.Day,
                    retainedFileCountLimit: 7,
                    outputTemplate: 
                    "{Timestamp:yyyy-MM-dd HH:mm:ss.fff zzz} [{Level:u3}] ({SourceContext}) {Message:lj}{NewLine}{Exception}");

            Log.Logger = loggerConfig.CreateLogger();
        }

        private Serilog.ILogger CreateSerilogLogger()
        {
            return Log.Logger.ForContext<LoggingService>();
        }

        public void SetLogLevel(Microsoft.Extensions.Logging.LogLevel level)
        {
            _levelSwitch.MinimumLevel = level switch
            {
                Microsoft.Extensions.Logging.LogLevel.Trace => Serilog.Events.LogEventLevel.Verbose,
                Microsoft.Extensions.Logging.LogLevel.Debug => Serilog.Events.LogEventLevel.Debug,
                Microsoft.Extensions.Logging.LogLevel.Information => Serilog.Events.LogEventLevel.Information,
                Microsoft.Extensions.Logging.LogLevel.Warning => Serilog.Events.LogEventLevel.Warning,
                Microsoft.Extensions.Logging.LogLevel.Error => Serilog.Events.LogEventLevel.Error,
                Microsoft.Extensions.Logging.LogLevel.Critical => Serilog.Events.LogEventLevel.Fatal,
                _ => Serilog.Events.LogEventLevel.Information
            };
        }
    }
}