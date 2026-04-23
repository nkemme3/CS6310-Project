import { useEffect, useState } from 'react';
import { Companies, Reports as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Reports() {
  const [companies, setCompanies] = useState([]);
  const [summary, setSummary] = useState(null);
  const [ledger, setLedger] = useState([]);
  const [sources, setSources] = useState(null);
  const [form, setForm] = useState({ companyShortName: '', from: '', to: '' });
  const toast = useToast();

  useEffect(() => { Companies.list().then(setCompanies).catch(() => {}); }, []);

  const load = (e) => {
    e.preventDefault();
    Api.summary(form.companyShortName, form.from || null, form.to || null)
      .then(setSummary)
      .catch((err) => toast.error(extractError(err)));
    Api.ledger(form.companyShortName)
      .then(setLedger)
      .catch((err) => toast.error(extractError(err)));
    Api.sources(form.companyShortName, form.from || null, form.to || null)
      .then(setSources)
      .catch(() => setSources(null));
  };

  return (
    <section>
      <h2>Reports</h2>
      <form className="form" onSubmit={load}>
        <select required value={form.companyShortName}
          onChange={(e) => setForm({ ...form, companyShortName: e.target.value })}>
          <option value="">Company</option>
          {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
        </select>
        <input type="date" value={form.from}
          onChange={(e) => setForm({ ...form, from: e.target.value })} />
        <input type="date" value={form.to}
          onChange={(e) => setForm({ ...form, to: e.target.value })} />
        <button type="submit">Load</button>
      </form>

      {summary && (
        <div className="card">
          <h3>{summary.companyShortName}</h3>
          <div>Revenue: ${summary.totalRevenue}</div>
          <div>Cost: ${summary.totalCost}</div>
          <div>Net income: ${summary.netIncome}</div>
          <div>kWh produced: {summary.totalKWhProduced}</div>
          <div>kWh billed: {summary.totalKWhBilled}</div>
        </div>
      )}

      {sources && sources.rows.length > 0 && (
        <div className="card">
          <h3>Generation mix</h3>
          <div>Total kWh: {String(sources.totalKWh)}</div>
          <div>Renewable kWh: {String(sources.renewableKWh)}</div>
          <div>Estimated carbon: {String(sources.totalEstimatedCarbonKg)} kg CO2</div>
          <table className="ledger">
            <thead>
              <tr><th>Source</th><th>Renewable</th><th>Plants</th><th>kWh</th><th>CO2 (kg)</th></tr>
            </thead>
            <tbody>
              {sources.rows.map((r) => (
                <tr key={r.source}>
                  <td>{r.source}</td>
                  <td>{r.renewable ? 'yes' : 'no'}</td>
                  <td>{r.plantCount}</td>
                  <td>{String(r.kWhProduced)}</td>
                  <td>{String(r.estimatedCarbonKg)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {ledger.length > 0 && (
        <>
          <h3>Ledger ({ledger.length} entries)</h3>
          <table className="ledger">
            <thead>
              <tr>
                <th>#</th><th>Type</th><th>Start</th><th>End</th><th>kWh</th><th>Amount</th><th>Description</th>
              </tr>
            </thead>
            <tbody>
              {ledger.map((e) => (
                <tr key={e.entryId}>
                  <td>{e.entryId}</td>
                  <td>{e.entryType}</td>
                  <td>{e.periodStart}</td>
                  <td>{e.periodEnd}</td>
                  <td>{e.kWh ?? '-'}</td>
                  <td>{e.amount ?? '-'}</td>
                  <td>{e.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </section>
  );
}
