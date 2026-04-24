import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

// Role gates mirror the backend @PreAuthorize matrix. Everyone signed in can
// read everything, so we just use these to decide whether to show sections
// that exist primarily to drive mutations.
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

  const onLogout = async () => {
    await logout();
    nav('/login', { replace: true });
  };

  return (
    <div className="layout">
      <aside className="nav">
        <h1>Power Grid</h1>
        <p className="subtitle">CS6310 Group 30</p>
        <nav>
          {links.map((l) => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              {l.label}
            </NavLink>
          ))}
        </nav>
        {user && (
          <div className="nav-user">
            <div className="nav-user-name">{user.username}</div>
            <div className="nav-user-roles">{user.roles.join(', ')}</div>
            <button type="button" className="nav-logout" onClick={onLogout}>Sign out</button>
          </div>
        )}
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
