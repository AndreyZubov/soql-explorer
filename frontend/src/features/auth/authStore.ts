import { create } from 'zustand';

/**
 * Authentication state kept in memory only.
 *
 * - Access token is held in memory (never in localStorage) to keep it out of
 *   reach of trivial XSS exfiltration. A page refresh wipes it; the
 *   /auth/refresh round-trip on first protected call rehydrates it from the
 *   HttpOnly refresh cookie.
 * - The lightweight profile (id, email, roles) is also kept in memory; the
 *   /me endpoint repopulates it after a refresh-driven bootstrap.
 */
export interface AuthProfile {
  userId: string;
  email: string;
  roles: string[];
}

interface AuthState {
  accessToken: string | null;
  profile: AuthProfile | null;
  setAccessToken: (token: string | null) => void;
  setProfile: (profile: AuthProfile | null) => void;
  setSession: (token: string, profile: AuthProfile) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  profile: null,
  setAccessToken: (accessToken) => set({ accessToken }),
  setProfile: (profile) => set({ profile }),
  setSession: (accessToken, profile) => set({ accessToken, profile }),
  clear: () => set({ accessToken: null, profile: null }),
}));
