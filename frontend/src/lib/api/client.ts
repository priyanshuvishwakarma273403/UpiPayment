import { ApiErrorResponse } from '../errors';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>;
  skipAuthRefresh?: boolean;
}

class HttpClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getAuthToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('sentinelx_token');
    }
    return null;
  }

  private getRefreshToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('sentinelx_refresh_token');
    }
    return null;
  }

  private setAuthTokens(accessToken: string, refreshToken?: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('sentinelx_token', accessToken);
      if (refreshToken) {
        localStorage.setItem('sentinelx_refresh_token', refreshToken);
      }
    }
  }

  private clearAuthTokens(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('sentinelx_token');
      localStorage.removeItem('sentinelx_refresh_token');
      localStorage.removeItem('sentinelx_user');
    }
  }

  private buildUrl(endpoint: string, params?: Record<string, string | number | boolean | undefined>): string {
    const url = new URL(endpoint.startsWith('http') ? endpoint : `${this.baseUrl}${endpoint}`);
    if (params) {
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null) {
          url.searchParams.append(key, String(val));
        }
      });
    }
    return url.toString();
  }

  public async request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    const { params, headers, skipAuthRefresh = false, ...customConfig } = options;
    const token = this.getAuthToken();

    const config: RequestInit = {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...headers,
      },
      ...customConfig,
    };

    const url = this.buildUrl(endpoint, params);

    try {
      const response = await fetch(url, config);

      if (response.status === 401 && !skipAuthRefresh && !endpoint.includes('/auth/')) {
        const refreshToken = this.getRefreshToken();
        if (refreshToken) {
          try {
            const refreshRes = await fetch(this.buildUrl('/auth/refresh-token'), {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ refreshToken }),
            });
            if (refreshRes.ok) {
              const refreshData = await refreshRes.json();
              const newToken = refreshData.accessToken || refreshData.token;
              if (newToken) {
                this.setAuthTokens(newToken, refreshData.refreshToken);
                // Retry original request with new token
                return this.request<T>(endpoint, { ...options, skipAuthRefresh: true });
              }
            }
          } catch {
            // Token refresh failed
          }
        }
        // If refresh failed or not present, clear session and redirect
        this.clearAuthTokens();
        if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
          window.location.href = '/unauthorized';
        }
      }

      if (!response.ok) {
        let errorData: Record<string, unknown> = {};
        try {
          errorData = await response.json();
        } catch {
          // Response body was not JSON
        }
        throw new ApiErrorResponse(
          (errorData.message as string) || `HTTP Error ${response.status}: ${response.statusText}`,
          response.status,
          (errorData.code as string) || 'HTTP_ERROR',
          errorData
        );
      }

      const data = await response.json();
      return data as T;
    } catch (error) {
      if (error instanceof ApiErrorResponse) {
        throw error;
      }
      throw new ApiErrorResponse(
        error instanceof Error ? error.message : 'Network request failed',
        500,
        'NETWORK_ERROR'
      );
    }
  }

  public get<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'GET' });
  }

  public post<T>(endpoint: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  public put<T>(endpoint: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  public delete<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'DELETE' });
  }
}

export const apiClient = new HttpClient(API_BASE_URL);
