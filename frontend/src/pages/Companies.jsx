import { useEffect, useState } from 'react';
import { Companies as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Companies() {
  const [list, setList] = useState([]);
  const [form, setForm] = useState({ longName: '', shortName: '', standardRate: '' });
  const toast = useToast();

  const refresh = () => Api.list().then(setList).catch((e) => toast.error(extractError(e)));
  useEffect(() => { refresh(); }, []);

  const submit = (e) => {
    e.preventDefault();
    Api.create({
      longName: form.longName,
      shortName: form.shortName,
      standardRate: Number(form.standardRate),
    })
      .then(() => {
        toast.success(`Created company ${form.shortName}`);
        setForm({ longName: '', shortName: '', standardRate: '' });
        refresh();
      })
      .catch((e) => toast.error(extractError(e)));
  };

  return (
    <section>
      <h2>Companies</h2>
      <form className="form" onSubmit={submit}>
        <input required placeholder="Long name" value={form.longName}
          onChange={(e) => setForm({ ...form, longName: e.target.value })} />
        <input required placeholder="Short name" value={form.shortName}
          onChange={(e) => setForm({ ...form, shortName: e.target.value })} />
        <input required type="number" step="0.0001" placeholder="Standard rate ($/kWh)"
          value={form.standardRate}
          onChange={(e) => setForm({ ...form, standardRate: e.target.value })} />
        <button type="submit">Create</button>
      </form>
      <ul className="card-list">
        {list.map((c) => (
          <li key={c.shortName} className="card">
            <strong>{c.longName}</strong> <span className="muted">({c.shortName})</span>
            <div>Standard rate: {c.standardRate} / kWh</div>
            <div className="muted">
              {c.plantIds.length} plants · {c.substationIds.length} substations ·
              {' '}{c.transformerIds.length} transformers · {c.customerAccountNumbers.length} customers
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
