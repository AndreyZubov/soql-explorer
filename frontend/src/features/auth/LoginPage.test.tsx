import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import LoginPage from './LoginPage';
import { useAuthStore } from './authStore';

// Mock the axios instance — we don't want a real network call from a unit test.
vi.mock('@/lib/api', () => ({
  api: {
    post: vi.fn(),
  },
}));

import { api } from '@/lib/api';

describe('LoginPage', () => {
  beforeEach(() => {
    useAuthStore.getState().clear();
    vi.mocked(api.post).mockReset();
  });

  it('submits credentials and hydrates the auth store on success', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({
      data: {
        accessToken: 'jwt.value',
        userId: 'u1',
        email: 'u@example.com',
        roles: ['USER'],
      },
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/connections" element={<div>connections-page</div>} />
        </Routes>
      </MemoryRouter>,
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'u@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'pw');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(useAuthStore.getState().accessToken).toBe('jwt.value');
    });
    expect(await screen.findByText(/connections-page/i)).toBeInTheDocument();
  });

  it('shows a generic error on 401', async () => {
    vi.mocked(api.post).mockRejectedValueOnce({ response: { status: 401 } });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'u@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'bad');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i);
    expect(useAuthStore.getState().accessToken).toBeNull();
  });
});
