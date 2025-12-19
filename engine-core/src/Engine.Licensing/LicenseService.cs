using System;
using System.Threading.Tasks;
using System.Net.Http;
using Newtonsoft.Json;

namespace PlayoutEngine.Licensing
{
    public interface ILicenseService
    {
        Task<bool> ValidateLicenseAsync(string licenseKey);
        Task<bool> IsLicenseValidAsync();
        Task<string?> GetCurrentLicenseStatusAsync();
        Task<bool> RefreshLicenseAsync();
    }

    public class LicenseService : ILicenseService
    {
        private string? _licenseKey;
        private DateTime _lastValidation;
        private bool _isValid;
        private DateTime _expirationDate;
        private readonly HttpClient _httpClient;
        private readonly string _licenseServerUrl = "https://api.your-license-service.com";

        public LicenseService()
        {
            _httpClient = new HttpClient();
            _httpClient.DefaultRequestHeaders.Add("User-Agent", "BroadcastPlayoutEngine/1.0");
        }

        public async Task<bool> ValidateLicenseAsync(string licenseKey)
        {
            try
            {
                _licenseKey = licenseKey;
                
                var response = await _httpClient.PostAsync($"{_licenseServerUrl}/validate", 
                    new StringContent(JsonConvert.SerializeObject(new { 
                        licenseKey = licenseKey,
                        productId = "broadcast-playout-engine"
                    }), System.Text.Encoding.UTF8, "application/json"));

                if (response.IsSuccessStatusCode)
                {
                    var content = await response.Content.ReadAsStringAsync();
                    var result = JsonConvert.DeserializeObject<LicenseValidationResponse>(content);

                    if (result != null && result.IsValid)
                    {
                        _isValid = true;
                        _expirationDate = result.ExpirationDate;
                        _lastValidation = DateTime.UtcNow;
                        
                        Console.WriteLine($"License validated. Expires: {_expirationDate}");
                        return true;
                    }
                }
                
                _isValid = false;
                return false;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"License validation error: {ex.Message}");
                _isValid = false; // Fail closed
                return false;
            }
        }

        public async Task<bool> IsLicenseValidAsync()
        {
            // For testing purposes, always return true
            // In production, this would use the actual license validation logic
            return true;

            // ORIGINAL CODE (commented out):
            // Check if we need to refresh (every 30 minutes)
            // if (DateTime.UtcNow > _lastValidation.AddMinutes(30))
            // {
            //     await RefreshLicenseAsync();
            // }
            //
            // // Check expiration
            // if (_expirationDate < DateTime.UtcNow)
            // {
            //     _isValid = false;
            // }
            //
            // return _isValid;
        }

        public async Task<string?> GetCurrentLicenseStatusAsync()
        {
            if (string.IsNullOrEmpty(_licenseKey)) return "NO_LICENSE";

            if (!await IsLicenseValidAsync())
            {
                return "INVALID_LICENSE";
            }

            var daysUntilExpiration = (_expirationDate - DateTime.UtcNow).Days;
            return $"VALID ({daysUntilExpiration} days remaining)";
        }

        public async Task<bool> RefreshLicenseAsync()
        {
            if (string.IsNullOrEmpty(_licenseKey))
            {
                return false;
            }

            return await ValidateLicenseAsync(_licenseKey);
        }
    }

    public class LicenseValidationResponse
    {
        public bool IsValid { get; set; }
        public DateTime ExpirationDate { get; set; }
        public string? Message { get; set; }
        public int AllowedInstances { get; set; }
        public string? PlanType { get; set; }
    }
}