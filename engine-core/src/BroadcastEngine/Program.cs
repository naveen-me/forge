using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using PlayoutEngine.Services;
using PlayoutEngine.OBS;
using PlayoutEngine.Licensing;
using PlayoutEngine.Logging;
using System.IO;

namespace PlayoutEngine
{
    class Program
    {
        static void Main(string[] args)
        {
            var hostBuilder = Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    webBuilder.UseStartup<Startup>();
                })
                .ConfigureAppConfiguration((hostingContext, config) =>
                {
                    // Find the project root by searching upward for the directory containing the 'config' folder
                    var currentDir = new DirectoryInfo(AppContext.BaseDirectory);
                    DirectoryInfo searchDir = currentDir;
                    string configPath = string.Empty;

                    // Search up to 10 directory levels to find the project root containing the 'config' directory
                    for (int i = 0; i < 10; i++)
                    {
                        var testConfigPath = Path.Combine(searchDir.FullName, "config");
                        if (Directory.Exists(testConfigPath))
                        {
                            configPath = testConfigPath;
                            break; // Found the config directory
                        }

                        if (searchDir.Parent != null)
                        {
                            searchDir = searchDir.Parent;
                        }
                        else
                        {
                            break; // Reached the system root
                        }
                    }

                    // If we get here, we couldn't find the config directory
                    if (string.IsNullOrEmpty(configPath))
                    {
                        throw new DirectoryNotFoundException($"Could not find 'config' directory in any parent directory of {AppContext.BaseDirectory}. Are you running from the correct location?");
                    }

                    config.SetBasePath(configPath);
                    config.AddJsonFile("engine.json", optional: false, reloadOnChange: true);
                })
                .ConfigureServices((hostContext, services) =>
                {
                    // Register configuration
                    services.AddSingleton(hostContext.Configuration);

                    // Register logging service
                    services.AddSingleton<ILoggingService, LoggingService>();

                    // Register core services
                    services.AddSingleton<ISchedulerService, SchedulerService>();
                    services.AddSingleton<ITimelineSchedulerService, TimelineSchedulerService>();
                    services.AddSingleton<IObsIntegrationService, ObsIntegrationService>();
                    services.AddSingleton<ILicenseService, LicenseService>();
                    services.AddSingleton<IHealthCheckService, HealthCheckService>();
                    services.AddSingleton<IBufferManager, BufferManager>();
                    services.AddSingleton<LogoService>();
                    services.AddSingleton<PlayoutArbiter>();

                    // Register the engine service as singleton so it can be injected into controllers
                    services.AddSingleton<PlayoutEngineService>();
                    // Also register it as a hosted service
                    services.AddHostedService(provider => provider.GetRequiredService<PlayoutEngineService>());
                })
                .ConfigureLogging(logging =>
                {
                    logging.ClearProviders();
                    // In a real implementation, we'd use the configured logging
                    logging.AddConsole();
                    logging.SetMinimumLevel(LogLevel.Information);
                });

            var host = hostBuilder.Build();

            // Configure logging with settings
            var loggingService = host.Services.GetRequiredService<ILoggingService>();
            loggingService.ConfigureLogging("");

            // Start the application
            host.Run();
        }
    }

    public class Startup
    {
        public IConfiguration Configuration { get; }

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public void ConfigureServices(IServiceCollection services)
        {
            services.AddControllers();
            services.AddRouting(options => options.LowercaseUrls = true);
        }

        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseRouting();
            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }
    }
}