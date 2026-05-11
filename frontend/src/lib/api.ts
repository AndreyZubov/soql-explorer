import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/features/auth/authStore';

// Single axios instance for the whole app.
//   - All requests go to /api (proxied to the backend by Vite in dev,
//     served from nginx in production).
//   - The access token is attached on each request from the auth store.
//   - On 401, we attempt a single refresh against /auth/refresh; if that
//     succeeds we replay the original request, otherwise the user is
//     forcibly logged out (the RequireAuth guard will redirect to /login).
export const api = axios.create({
  baseURL: '/api',
  withCredentials: true, // Needed so the refresh cookie is sent.
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

type RetryConfig = AxiosRequestConfig & { _retry?: boolean };

let inFlightRefresh: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  if (!inFlightRefresh) {
    inFlightRefresh = axios
      .post<{ accessToken: string; accessTokenExpiresAt: string }>(
        '/api/auth/refresh',
        {},
        { withCredentials: true },
      )
      .then((res) => {
        useAuthStore.getState().setAccessToken(res.data.accessToken);
        return res.data.accessToken;
      })
      .catch(() => {
        useAuthStore.getState().clear();
        return null;
      })
      .finally(() => {
        inFlightRefresh = null;
      });
  }
  return inFlightRefresh;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetryConfig | undefined;
    if (!original || error.response?.status !== 401 || original._retry) {
      return Promise.reject(error);
    }
    // Don't try to refresh on the refresh endpoint itself — that would loop.
    if (original.url?.includes('/auth/refresh') || original.url?.includes('/auth/login')) {
      return Promise.reject(error);
    }
    original._retry = true;
    const newToken = await refreshAccessToken();
    if (!newToken) {
      return Promise.reject(error);
    }
    original.headers = original.headers ?? {};
    (original.headers as Record<string, string>).Authorization = `Bearer ${newToken}`;
    return api.request(original);
  },
);
