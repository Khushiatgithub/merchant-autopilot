const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8091/api';
export const api = {
  token: () => localStorage.getItem('merchant_token'),
  async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    let response: Response;
    try {
      response = await fetch(`${API_URL}${path}`, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...(api.token() ? { Authorization: `Bearer ${api.token()}` } : {}),
          ...options.headers,
        },
      });
    } catch (error) {
      throw new Error(`Unable to reach the Merchant Autopilot backend at ${API_URL}`);
    }

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('merchant_token');
      }
      throw new Error(`API request failed (${response.status})`);
    }

    const text = await response.text();
    if (!text) return undefined as unknown as T;

    const payload = JSON.parse(text) as T & { value?: unknown };
    if (payload && typeof payload === 'object' && 'value' in payload && Array.isArray((payload as { value?: unknown }).value)) {
      return (payload as { value: T }).value;
    }

    return payload;
  },
  login: (email: string, password: string) => api.request<{ token: string; merchantName: string; email: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  customer: (id: string) => api.request<{ id: string; name: string; segment: string; avgOrderValue: number; favoriteCategory: string; nextPredictedProduct: string; confidenceScore: number }>('/customers/' + id),
  dashboard: () => api.request<Record<string, number>>('/dashboard'),
  plan: (body: object) => api.request<Record<string, unknown>>('/goals', { method: 'POST', body: JSON.stringify(body) }),
  customers: () => api.request<unknown[]>('/customers'),
  campaigns: () => api.request<unknown[]>('/campaigns'),
  audit: () => api.request<unknown[]>('/audit'),
  createPaymentLink: (body: object) => api.request<Record<string, unknown>>('/payments/create-link', { method: 'POST', body: JSON.stringify(body) }),
  verifyPayment: (body: object) => api.request<Record<string, unknown>>('/payments/verify', { method: 'POST', body: JSON.stringify(body) }),
};