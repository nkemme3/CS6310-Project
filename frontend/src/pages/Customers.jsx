import { useEffect, useState } from 'react';
import { Companies, Customers as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Customers() {
  const [list, setList] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [form, setForm] = useState({ companyShortName: '', name: '', customerType: 'RESIDENTIAL', x: '', y: '' });
  const [conn, setConn] = useState({ accountNumber: '', transformerId: '' });
  const toast = useToast();

  const refresh = () => Api.list().then(setList).catch(() => {});
  useEffect(() => {
    Companies.list().then(setCompanies).catch(() => {});
    refresh();
  }, []);

  const create = (e) => {
    e.preventDefault();
    Api.create({
      companyShortName: form.companyShortName,
      name: form.name,
      customerType: form.customerType,
      location: { x: Number(form.x), y: Number(form.y) },
    })
      .then(() => {
        toast.success(`Customer ${form.name} created.`);
        setForm({ companyShortName: '', name: '', customerType: 'RESIDENTIAL', x: '', y: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  const connect = (e) => {
    e.preventDefault();
    Api.connect(Number(conn.accountNumber), conn.transformerId)
      .then(() => {
        toast.success(`Account ${conn.accountNumber} connected to ${conn.transformerId}`);
        setConn({ accountNumber: '', transformerId: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  return (
    <section>
      <h2>Customers</h2>
      <div className="grid">
        <form className="form stacked" onSubmit={create}>
          <h4>Create customer</h4>
          <select required value={form.companyShortName}
            onChange={(e) => setForm({ ...form, companyShortName: e.target.value })}>
            <option value="">Select company</option>
            {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
          </select>
          <input required placeholder="Name" value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <select value={form.customerType}
            onChange={(e) => setForm({ ...form, customerType: e.target.value })}>
            <option value="RESIDENTIAL">RESIDENTIAL</option>
            <option value="COMMERCIAL">COMMERCIAL</option>
          </select>
          <div className="row">
            <input required type="number" placeholder="x" value={form.x}
              onChange={(e) => setForm({ ...form, x: e.target.value })} />
            <input required type="number" placeholder="y" value={form.y}
              onChange={(e) => setForm({ ...form, y: e.target.value })} />
          </div>
          <button type="submit">Create</button>
        </form>
        <form className="form stacked" onSubmit={connect}>
          <h4>Connect customer to transformer</h4>
          <input required type="number" placeholder="Account number" value={conn.accountNumber}
            onChange={(e) => setConn({ ...conn, accountNumber: e.target.value })} />
          <input required placeholder="Transformer ID" value={conn.transformerId}
            onChange={(e) => setConn({ ...conn, transformerId: e.target.value })} />
          <button type="submit">Connect</button>
        </form>
      </div>

      <h3>All customers</h3>
      <ul className="card-list">
        {list.map((c) => (
          <li key={c.accountNumber} className="card">
            <strong>#{c.accountNumber}</strong> {c.name} <span className="muted">({c.customerType})</span>
            <div className="muted">
              {c.companyShortName} · ({c.location.x},{c.location.y}) ·
              {' '}transformer: {c.connectedTransformerId ?? 'unconnected'}
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
