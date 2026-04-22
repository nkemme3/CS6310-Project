import { useEffect, useState } from 'react';
import { Billing as Api, Companies, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Billing() {
  const [companies, setCompanies] = useState([]);
  const [usage, setUsage] = useState({ accountNumber: '', periodStart: '', periodEnd: '', kWh: '' });
  const [production, setProduction] = useState({ plantId: '', periodStart: '', periodEnd: '', kWh: '' });
  const [cycle, setCycle] = useState({ companyShortName: '', periodStart: '', periodEnd: '' });
  const [cycleResult, setCycleResult] = useState(null);
  const toast = useToast();

  useEffect(() => { Companies.list().then(setCompanies).catch(() => {}); }, []);

  const recordUsage = (e) => {
    e.preventDefault();
    Api.recordUsage({
      accountNumber: Number(usage.accountNumber),
      periodStart: usage.periodStart, periodEnd: usage.periodEnd,
      kWh: Number(usage.kWh),
    })
      .then(() => toast.success('Usage recorded.'))
      .catch((err) => toast.error(extractError(err)));
  };

  const recordProduction = (e) => {
    e.preventDefault();
    Api.recordProduction({
      plantId: production.plantId,
      periodStart: production.periodStart, periodEnd: production.periodEnd,
      kWh: Number(production.kWh),
    })
      .then(() => toast.success('Production recorded.'))
      .catch((err) => toast.error(extractError(err)));
  };

  const runCycle = (e) => {
    e.preventDefault();
    Api.run({
      companyShortName: cycle.companyShortName,
      periodStart: cycle.periodStart, periodEnd: cycle.periodEnd,
    })
      .then((resp) => {
        setCycleResult(resp);
        toast.success(`Cycle complete — ${resp.entries.length} ledger entries.`);
      })
      .catch((err) => toast.error(extractError(err)));
  };

  return (
    <section>
      <h2>Billing</h2>
      <div className="grid">
        <form className="form stacked" onSubmit={recordUsage}>
          <h4>Record customer usage</h4>
          <input required type="number" placeholder="Account #" value={usage.accountNumber}
            onChange={(e) => setUsage({ ...usage, accountNumber: e.target.value })} />
          <div className="row">
            <input required type="date" value={usage.periodStart}
              onChange={(e) => setUsage({ ...usage, periodStart: e.target.value })} />
            <input required type="date" value={usage.periodEnd}
              onChange={(e) => setUsage({ ...usage, periodEnd: e.target.value })} />
          </div>
          <input required type="number" step="0.01" placeholder="kWh" value={usage.kWh}
            onChange={(e) => setUsage({ ...usage, kWh: e.target.value })} />
          <button type="submit">Record</button>
        </form>
        <form className="form stacked" onSubmit={recordProduction}>
          <h4>Record plant production</h4>
          <input required placeholder="Plant ID" value={production.plantId}
            onChange={(e) => setProduction({ ...production, plantId: e.target.value })} />
          <div className="row">
            <input required type="date" value={production.periodStart}
              onChange={(e) => setProduction({ ...production, periodStart: e.target.value })} />
            <input required type="date" value={production.periodEnd}
              onChange={(e) => setProduction({ ...production, periodEnd: e.target.value })} />
          </div>
          <input required type="number" step="0.01" placeholder="kWh"
            value={production.kWh}
            onChange={(e) => setProduction({ ...production, kWh: e.target.value })} />
          <button type="submit">Record</button>
        </form>
        <form className="form stacked" onSubmit={runCycle}>
          <h4>Run billing cycle</h4>
          <select required value={cycle.companyShortName}
            onChange={(e) => setCycle({ ...cycle, companyShortName: e.target.value })}>
            <option value="">Company</option>
            {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
          </select>
          <div className="row">
            <input required type="date" value={cycle.periodStart}
              onChange={(e) => setCycle({ ...cycle, periodStart: e.target.value })} />
            <input required type="date" value={cycle.periodEnd}
              onChange={(e) => setCycle({ ...cycle, periodEnd: e.target.value })} />
          </div>
          <button type="submit">Run</button>
        </form>
      </div>

      {cycleResult && (
        <>
          <h3>Cycle output</h3>
          <ul className="card-list">
            {cycleResult.entries.map((e) => (
              <li key={e.entryId} className="card">
                <strong>{e.entryType}</strong> · {e.companyShortName} ·
                {' '}{e.periodStart} → {e.periodEnd}
                <div className="muted">
                  {e.kWh && <>kWh: {e.kWh} · </>}
                  {e.amount && <>amount: ${e.amount} · </>}
                  {e.description}
                </div>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  );
}
