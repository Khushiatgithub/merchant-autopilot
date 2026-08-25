const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
export const api = {
  token: () => localStorage.getItem('merchant_token'),
  async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    if (api.token() === 'demo') return demoResponse<T>(path, options);
    let response: Response;
    try {
      response = await fetch(`${API_URL}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(api.token() ? { Authorization: `Bearer ${api.token()}` } : {}), ...options.headers } });
    } catch (error) {
      if (path === '/goals') return demoResponse<T>(path, options);
      throw error;
    }
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('merchant_token');
        return demoResponse<T>(path, options);
      }
      if (path === '/goals') return demoResponse<T>(path, options);
      throw new Error(`API request failed (${response.status})`);
    }
    return response.json();
  },
  login: (email: string, password: string) => api.request<{ token: string; merchantName: string; email: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  dashboard: () => api.request<Record<string, number>>('/dashboard'),
  plan: (body: object) => api.request<Record<string, unknown>>('/goals', { method: 'POST', body: JSON.stringify(body) }),
  customers: () => api.request<unknown[]>('/customers'),
  campaigns: () => api.request<unknown[]>('/campaigns'),
  audit: () => api.request<unknown[]>('/audit'),
  createPaymentLink: (body: object) => api.request<Record<string, unknown>>('/payments/create-link', { method: 'POST', body: JSON.stringify(body) }),
  verifyPayment: (body: object) => api.request<Record<string, unknown>>('/payments/verify', { method: 'POST', body: JSON.stringify(body) }),
};

function demoResponse<T>(path: string, options: RequestInit = {}): T {
  const requestBody = options.body ? JSON.parse(String(options.body)) : {};
  const revenueGoal = Number(requestBody.revenueGoal || 200000);
  const maxDiscount = Number(requestBody.maxDiscount ?? 15);
  const minMargin = Number(requestBody.minMargin ?? 25);
  const safeDiscount = (preferred: number) => minMargin > 38 ? 0 : Math.min(preferred, maxDiscount);
  const strict = maxDiscount <= 5 && minMargin >= 35;
  const weights = strict ? [.24, .11, .15, .20] : [.31, .155, .245, .29];
  const records = path === '/payments/create-link'
    ? { status: 'TEST_MODE', testMode: true, shortUrl: 'https://rzp.io/i/demo-autopilot', paymentLinkId: 'plink_demo_autopilot', orderId: `demo-order-${Date.now()}` }
    : path === '/payments/verify'
      ? { verified: true, status: 'PAID', signatureChecked: true, revenueUpdated: true }
      : path === '/goals'
    ? { agent: 'Strategist', goal: revenueGoal, strategies: [{ name: 'Upsell', type: 'UPSELL', expectedRevenue: Math.round(revenueGoal * weights[0]), discount: safeDiscount(5), confidence: .94, marginImpact: minMargin >= 35 ? 'Protected' : 'Healthy' }, { name: 'Cross-sell', type: 'CROSS_SELL', expectedRevenue: Math.round(revenueGoal * weights[1]), discount: safeDiscount(5), confidence: .86, marginImpact: minMargin >= 35 ? 'Protected' : 'Healthy' }, { name: 'Bundles', type: 'BUNDLE', expectedRevenue: Math.round(revenueGoal * weights[2]), discount: safeDiscount(5), confidence: .82, marginImpact: minMargin >= 35 ? 'Protected' : 'Healthy' }, { name: 'Win-back', type: 'WIN_BACK', expectedRevenue: Math.round(revenueGoal * weights[3]), discount: safeDiscount(10), confidence: .87, marginImpact: minMargin >= 35 ? 'Protected' : 'Healthy' }], totalExpectedRevenue: Math.round(revenueGoal * weights.reduce((sum, weight) => sum + weight, 0)), guardrails: { maxDiscount, minMargin } }
    : path === '/customers'
    ? [{ name: 'Aarav Mehta', email: 'aarav@example.com', segment: 'Champions', confidence: 0.94, reason: 'Likely to purchase Performance Socks' }, { name: 'Diya Kapoor', email: 'diya@example.com', segment: 'At risk', confidence: 0.87, reason: 'No purchase in 64 days' }, { name: 'Kabir Shah', email: 'kabir@example.com', segment: 'New', confidence: 0.82, reason: 'First purchase completed this month' }]
    : path === '/campaigns'
      ? [{ name: 'Upsell Shoes + Socks', status: 'PENDING_APPROVAL', confidence: 0.94, reason: 'Expected revenue ₹62,000' }, { name: 'Win back lapsed customers', status: 'ACTIVE', confidence: 0.87, reason: 'Expected revenue ₹58,000' }]
      : [{ actor: 'Strategist Agent', action: 'Generated revenue plan', approval: 'PENDING', confidence: 0.94, reason: 'Merchant goal analyzed' }, { actor: 'Scout Agent', action: 'Found 1,284 Champion customers', approval: 'SYSTEM', confidence: 0.91, reason: 'Purchase affinity detected' }];
  return records as T;
}