import { useEffect, useState } from 'react';
import { Companies, Employees as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Employees() {
  const [list, setList] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [form, setForm] = useState({ companyShortName: '', employeeId: '', name: '', startDate: '', hourlyWage: '' });
  const toast = useToast();

  const refresh = () => Api.list().then(setList).catch(() => {});
  useEffect(() => {
    Companies.list().then(setCompanies).catch(() => {});
    refresh();
  }, []);

  const submit = (e) => {
    e.preventDefault();
    Api.create({
      companyShortName: form.companyShortName,
      employeeId: form.employeeId,
      name: form.name,
      startDate: form.startDate || null,
      hourlyWage: Number(form.hourlyWage),
    })
      .then(() => {
        toast.success(`Employee ${form.employeeId} created.`);
        setForm({ companyShortName: '', employeeId: '', name: '', startDate: '', hourlyWage: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  return (
    <section>
      <h2>Employees</h2>
      <form className="form" onSubmit={submit}>
        <select required value={form.companyShortName}
          onChange={(e) => setForm({ ...form, companyShortName: e.target.value })}>
          <option value="">Company</option>
          {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
        </select>
        <input required placeholder="Employee ID" value={form.employeeId}
          onChange={(e) => setForm({ ...form, employeeId: e.target.value })} />
        <input required placeholder="Name" value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input type="date" value={form.startDate}
          onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
        <input required type="number" step="0.01" placeholder="Hourly wage"
          value={form.hourlyWage}
          onChange={(e) => setForm({ ...form, hourlyWage: e.target.value })} />
        <button type="submit">Create</button>
      </form>
      <ul className="card-list">
        {list.map((e) => (
          <li key={e.employeeId} className="card">
            <strong>{e.name}</strong> <span className="muted">({e.employeeId})</span>
            <div className="muted">{e.companyShortName} · start {e.startDate} · ${e.hourlyWage}/hr</div>
          </li>
        ))}
      </ul>
    </section>
  );
}
