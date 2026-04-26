import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { getHealth } from '../api/client.js';

const links = [
  { to: '/',               label: 'Overview',       end: true },
  { to: '/companies',      label: 'Companies' },
  { to: '/infrastructure', label: 'Infra' },
  { to: '/customers',      label: 'Customers' },
  { to: '/employees',      label: 'Crew' },
  { to: '/issues',         label: 'Issues' },
  { to: '/rate-plans',     label: 'Tariffs' },
  { to: '/billing',        label: 'Billing' },
  { to: '/reports',        label: 'Reports' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const [online, setOnline] = useState(true);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    let cancelled = false;
    const ping = () => {
      getHealth().then(() => !cancelled && setOnline(true))
                 .catch(() => !cancelled && setOnline(false));
    };
    ping();
    const id = setInterval(ping, 15000);
    const tick = setInterval(() => setNow(new Date()), 1000);
    return () => { cancelled = true; clearInterval(id); clearInterval(tick); };
  }, []);

  const onLogout = async () => {
    await logout();
    nav('/login', { replace: true });
  };

  const ts = now.toISOString().slice(0, 19).replace('T', ' ');

  return (
    <div className="layout">
      <header className="nav">
        <div className="nav-brand">
          <h1>EPD Console</h1>
          <p className="subtitle">Electric Power Distribution // Group 30</p>
        </div>
        <nav>
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              {l.label}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
      <footer className="status-strip">
        <div className="left">
          <span><span className="dot" style={{ background: online ? 'var(--moss)' : 'var(--rust)' }} />
            {online ? 'LINK OK' : 'LINK DOWN'}
          </span>
          <span>UTC {ts}</span>
        </div>
        <div className="right">
          {user && (
            <>
              <span>OPERATOR <strong>{user.username}</strong></span>
              <span className="pill">{user.roles.join(' · ')}</span>
              <button type="button" className="status-logout" onClick={onLogout}>Sign out</button>
            </>
          )}
        </div>
      </footer>
    </div>
  );
}
