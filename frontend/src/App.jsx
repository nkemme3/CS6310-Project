import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import { ToastProvider } from './components/Toast.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Companies from './pages/Companies.jsx';
import Infrastructure from './pages/Infrastructure.jsx';
import Customers from './pages/Customers.jsx';
import Employees from './pages/Employees.jsx';
import Issues from './pages/Issues.jsx';
import RatePlans from './pages/RatePlans.jsx';
import Billing from './pages/Billing.jsx';
import Reports from './pages/Reports.jsx';

export default function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
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
      </BrowserRouter>
    </ToastProvider>
  );
}
