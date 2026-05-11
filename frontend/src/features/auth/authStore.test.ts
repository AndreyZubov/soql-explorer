import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
  });

  it('starts with no token and no profile', () => {
    const state = useAuthStore.getState();
    expect(state.accessToken).toBeNull();
    expect(state.profile).toBeNull();
  });

  it('setSession populates both token and profile', () => {
    useAuthStore.getState().setSession('jwt.value', {
      userId: 'u1',
      email: 'u@example.com',
      roles: ['USER'],
    });
    const state = useAuthStore.getState();
    expect(state.accessToken).toBe('jwt.value');
    expect(state.profile?.email).toBe('u@example.com');
  });

  it('clear wipes the session', () => {
    useAuthStore.getState().setSession('jwt.value', {
      userId: 'u1',
      email: 'u@example.com',
      roles: ['USER'],
    });
    useAuthStore.getState().clear();
    const state = useAuthStore.getState();
    expect(state.accessToken).toBeNull();
    expect(state.profile).toBeNull();
  });
});
