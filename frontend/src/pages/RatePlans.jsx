import { useEffect, useState } from 'react';
import { Companies, RatePlans as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function RatePlans() {
  const [list, setList] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [form, setForm] = useState({
    planId: '', companyShortName: '', ratePerKWh: '',
    customerType: 'RESIDENTIAL', accountNumber: '',
    effectiveStart: '', effectiveEnd: '',
  });
  const toast = useToast();

  const refresh = () => Api.list().then(setList).catch(() => {});
  useEffect(() => {
    Companies.list().then(setCompanies).catch(() => {});
    refresh();
  }, []);

  const submit = (e) => {
    e.preventDefault();
    Api.create({
      planId: form.planId,
      companyShortName: form.companyShortName,
      ratePerKWh: Number(form.ratePerKWh),
      customerType: form.customerType,
      accountNumber: form.accountNumber ? Number(form.accountNumber) : null,
      effectiveStart: form.effectiveStart,
      effectiveEnd: form.effectiveEnd,
    })
      .then(() => {
        toast.success(`Rate plan ${form.planId} created.`);
        setForm({ planId: '', companyShortName: '', ratePerKWh: '', customerType: 'RESIDENTIAL',
          accountNumber: '', effectiveStart: '', effectiveEnd: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  return (
    <section>
      <h2>Rate plans</h2>
      <form className="form" onSubmit={submit}>
        <input required placeholder="Plan ID" value={form.planId}
          onChange={(e) => setForm({ ...form, planId: e.target.value })} />
        <select required value={form.companyShortName}
          onChange={(e) => setForm({ ...form, companyShortName: e.target.value })}>
          <option value="">Company</option>
          {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
        </select>
        <input required type="number" step="0.0001" placeholder="Rate / kWh"
          value={form.ratePerKWh}
          onChange={(e) => setForm({ ...form, ratePerKWh: e.target.value })} />
        <select value={form.customerType}
          onChange={(e) => setForm({ ...form, customerType: e.target.value })}>
          <option value="RESIDENTIAL">RESIDENTIAL</option>
          <option value="COMMERCIAL">COMMERCIAL</option>
        </select>
        <input type="number" placeholder="Account # (optional)" value={form.accountNumber}
          onChange={(e) => setForm({ ...form, accountNumber: e.target.value })} />
        <input required type="date" value={form.effectiveStart}
          onChange={(e) => setForm({ ...form, effectiveStart: e.target.value })} />
        <input required type="date" value={form.effectiveEnd}
          onChange={(e) => setForm({ ...form, effectiveEnd: e.target.value })} />
        <button type="submit">Create</button>
      </form>
      <ul className="card-list">
        {list.map((p) => (
          <li key={p.planId} className="card">
            <strong>{p.planId}</strong> · {p.companyShortName} · ${p.ratePerKWh}/kWh ·
            {' '}{p.customerType}
            {p.accountNumber && <> · account #{p.accountNumber}</>}
            <div className="muted">{p.effectiveStart} → {p.effectiveEnd}</div>
          </li>
        ))}
      </ul>
    </section>
  );
}
