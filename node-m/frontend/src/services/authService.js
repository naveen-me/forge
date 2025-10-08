const TOKEN_KEY = 'authToken';
const PHP_TOKEN_KEY = 'phpAuthToken'; // Store PHP token separately if needed

class AuthService {

  async login(email, password) {
    try {
      const response = await window.electronAPI.api.call('POST', '/auth/login', { email, password });
      if (response.success && response.data && response.data.data && response.data.data.token) {
        localStorage.setItem(TOKEN_KEY, response.data.data.token);
        // Store PHP token if returned by backend
        if (response.data.data.phpToken) {
          localStorage.setItem(PHP_TOKEN_KEY, response.data.data.phpToken);
        }
        return { success: true, data: response.data.data };
      }
      return { success: false, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : (response && response.message ? response.message : 'No token received')) };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: error && error.message ? error.message : 'Login failed' };
    }
  }

  async register(name, email, password) {
    try {
      const response = await window.electronAPI.api.call('POST', '/auth/register', { name, email, password });
      if (response.success && response.data && response.data.data && response.data.data.token) {
        localStorage.setItem(TOKEN_KEY, response.data.data.token);
        // Store PHP token if returned by backend
        if (response.data.data.phpToken) {
          localStorage.setItem(PHP_TOKEN_KEY, response.data.data.phpToken);
        }
        return { success: true, data: response.data.data };
      }
      return { success: false, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : (response && response.message ? response.message : 'Registration failed')) };
    } catch (error) {
      console.error('Registration error:', error);
      return { success: false, message: error && error.message ? error.message : 'Registration failed' };
    }
  }

  async setPin(pin) {
    try {
      const nodeToken = this.getToken(); // This is the local Node token
      if (!nodeToken) {
        throw new Error('No authentication token found');
      }
      
      // Use the Node token for the request to Node backend
      // The Node backend will handle mapping to PHP token
      const response = await window.electronAPI.api.call('POST', '/auth/set-pin', { token: nodeToken, pin });
      
      if (response.success) {
        localStorage.setItem('user_pin', pin);
        return { success: true };
      }
      return { success: false, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : 'Failed to set PIN') };
    } catch (error) {
      console.error('Set PIN error:', error);
      return { success: false, message: error && error.message ? error.message : 'Set PIN failed' };
    }
  }

  async validatePin(pin) {
    try {
      const storedPin = localStorage.getItem('user_pin');
      if (storedPin && storedPin === pin) {
        return { success: true, message: 'PIN validated successfully' };
      }
      
      const nodeToken = this.getToken(); // This is the local Node token
      if (!nodeToken) {
        throw new Error('No authentication token found');
      }
      
      // Use the Node token for the request to Node backend
      const response = await window.electronAPI.api.call('POST', '/auth/validate-pin', { token: nodeToken, pin });
      
      if (response.success) {
        localStorage.setItem('user_pin', pin);
        return { success: true, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : 'PIN validated successfully') };
      }
      return { success: false, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : 'Invalid PIN') };
    } catch (error) {
      console.error('Validate PIN error:', error);
      return { success: false, message: error && error.message ? error.message : 'PIN validation failed' };
    }
  }

  async validateToken() {
    try {
      const nodeToken = this.getToken(); // This is the local Node token
      if (!nodeToken) {
        return { success: false, message: 'No local token found' };
      }
      
      // Validate the local token by making a request to the backend
      const response = await window.electronAPI.api.call('POST', '/auth/validate-token', { 
        token: nodeToken 
      });
      
      if (response && response.success && response.data && response.data.data) {
        return response.data.data;
      } else {
        return { success: false, message: response && response.data && response.data.data && response.data.data.message ? response.data.data.message : (response && response.data && response.data.message ? response.data.message : 'Token validation failed') };
      }
    } catch (error) {
      console.error('Token validation error:', error);
      return { success: false, message: error && error.message ? error.message : 'Token validation failed' };
    }
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('user_pin');
    window.location.hash = '#/login';
  }

  isAuthenticated() {
    const token = localStorage.getItem(TOKEN_KEY);
    return token !== null;
  }

  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  async checkConnectivity() {
    try {
      await fetch('https://matrixapi.io/', { method: 'HEAD' });
      return true;
    } catch (error) {
      console.error('Connectivity check failed:', error);
      return false;
    }
  }

  async saveTokenAndPin(token, pin) {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    }
    if (pin) {
      localStorage.setItem('user_pin', pin);
    }
  }

  async hasPin() {
    return localStorage.getItem('user_pin') !== null;
  }

  async isPinRequired() {
    return localStorage.getItem('user_pin') !== null;
  }
}

export default new AuthService();