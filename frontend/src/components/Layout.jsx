import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { getHealth } from '../api/client.js';

const links = [
  { to: '/',               label: 'Dashboard',      end: true },
  { to: '/companies',      label: 'Companies' },
  { to: '/infrastructure', label: 'Infrastructure' },
  { to: '/customers',      label: 'Customers' },
  { to: '/employees',      label: 'Employees' },
  { to: '/issues',         label: 'Issues' },
  { to: '/rate-plans',     label: 'Rate Plans' },
  { to: '/billing',        label: 'Billing' },
  { to: '/reports',        label: 'Reports' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const [online, setOnline] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const ping = () => {
      getHealth().then(() => !cancelled && setOnline(true))
                 .catch(() => !cancelled && setOnline(false));
    };
    ping();
    const id = setInterval(ping, 15000);
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  const onLogout = async () => {
    await logout();
    nav('/login', { replace: true });
  };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark" />
          <div>
            <h1>PowerGrid Console</h1>
            <p className="brand-sub">Group 30 &middot;</p>
          </div>
        </div>
        <nav>
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              {l.label}
            </NavLink>
          ))}
        </nav>
        {user && (
          <div className="user-block">
            <div className="user-row">
              <span className={`status-dot ${online ? 'on' : 'off'}`} />
              <span className="muted small">{online ? 'Backend online' : 'Backend offline'}</span>
            </div>
            <div className="user-name">{user.username}</div>
            <div className="user-roles">{user.roles.join(', ')}</div>
            <button type="button" className="logout-btn" onClick={onLogout}>Sign out</button>
          </div>
        )}
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
