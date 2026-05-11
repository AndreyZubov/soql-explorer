import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { api } from '@/lib/api';
import { useAuthStore } from '@/features/auth/authStore';
import { cn } from '@/lib/cn';

const links = [
  { to: '/connections', label: 'Connections' },
  { to: '/history', label: 'History' },
  { to: '/templates', label: 'Templates' },
];

// Authenticated-shell chrome. The schema browser and query editor get
// nested inside /explorer/:connectionId in Step 3.
export default function AppLayout() {
  const navigate = useNavigate();
  const profile = useAuthStore((s) => s.profile);
  const clear = useAuthStore((s) => s.clear);

  const onLogout = async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      clear();
      navigate('/login', { replace: true });
    }
  };

  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex items-center justify-between border-b border-border px-6 py-3">
        <div className="flex items-center gap-6">
          <span className="font-semibold">SOQL Explorer</span>
          <nav className="flex gap-4 text-sm">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  cn(
                    'text-muted-foreground hover:text-foreground',
                    isActive && 'font-medium text-foreground',
                  )
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-muted-foreground">{profile?.email}</span>
          <button
            type="button"
            onClick={onLogout}
            className="rounded-md border border-border px-3 py-1 hover:bg-muted"
          >
            Sign out
          </button>
        </div>
      </header>
      <main className="flex-1 p-6">
        <Outlet />
      </main>
    </div>
  );
}
