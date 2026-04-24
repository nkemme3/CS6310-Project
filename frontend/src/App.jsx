import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import { ToastProvider } from './components/Toast.jsx';
import { AuthProvider, useAuth } from './auth/AuthContext.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Companies from './pages/Companies.jsx';
import Infrastructure from './pages/Infrastructure.jsx';
import Customers from './pages/Customers.jsx';
import Employees from './pages/Employees.jsx';
import Issues from './pages/Issues.jsx';
import RatePlans from './pages/RatePlans.jsx';
import Billing from './pages/Billing.jsx';
import Reports from './pages/Reports.jsx';

function RequireAuth({ children }) {
  const { user, ready } = useAuth();
  const loc = useLocation();
  if (!ready) return <div className="loading">Loading…</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  return children;
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<RequireAuth><Layout /></RequireAuth>}>
            <Route index element={<Dashboard />} />
            <Route path="companies" element={<Companies />} />
            <Route path="infrastructure" element={<Infrastructure />} />
            <Route path="customers" element={<Customers />} />
            <Route path="employees" element={<Employees />} />
            <Route path="issues" element={<Issues />} />
            <Route path="rate-plans" element={<RatePlans />} />
            <Route path="billing" element={<Billing />} />
            <Route path="reports" element={<Reports />} />
          </Route>
        </Routes>
      </ToastProvider>
    </AuthProvider>
  );
}
