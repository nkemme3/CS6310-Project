import { NavLink, Outlet } from 'react-router-dom';

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/companies', label: 'Companies' },
  { to: '/infrastructure', label: 'Infrastructure' },
  { to: '/customers', label: 'Customers' },
  { to: '/employees', label: 'Employees' },
  { to: '/issues', label: 'Issues' },
  { to: '/rate-plans', label: 'Rate Plans' },
  { to: '/billing', label: 'Billing' },
  { to: '/reports', label: 'Reports' },
];

export default function Layout() {
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
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
