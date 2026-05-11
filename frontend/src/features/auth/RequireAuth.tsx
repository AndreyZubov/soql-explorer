import { type ReactNode, useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { api } from '@/lib/api';
import { useAuthStore, type AuthProfile } from './authStore';

interface Props {
  children: ReactNode;
}

/**
 * Route guard that bootstraps the session on first render.
 *
 * On a fresh page load the in-memory access token is empty. We attempt one
 * silent /auth/refresh call backed by the HttpOnly refresh cookie; if it
 * succeeds, we hydrate the auth store and let the route render. Otherwise
 * we redirect to /login, preserving the originally requested location.
 */
export default function RequireAuth({ children }: Props) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const setSession = useAuthStore((s) => s.setSession);
  const location = useLocation();
  const [bootstrapped, setBootstrapped] = useState(accessToken !== null);

  useEffect(() => {
    if (accessToken) {
      setBootstrapped(true);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const { data } = await api.post<{
          accessToken: string;
          userId: string;
          email: string;
          roles: string[];
        }>('/auth/refresh', {});
        if (!cancelled) {
          const profile: AuthProfile = {
            userId: data.userId,
            email: data.email,
            roles: data.roles,
          };
          setSession(data.accessToken, profile);
        }
      } catch {
        // Falling through — the redirect below handles it.
      } finally {
        if (!cancelled) {
          setBootstrapped(true);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [accessToken, setSession]);

  if (!bootstrapped) {
    return null;
  }
  if (!useAuthStore.getState().accessToken) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <>{children}</>;
}
