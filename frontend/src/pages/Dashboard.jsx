import { useEffect, useState } from 'react';
import { getHealth, Companies, Reports } from '../api/client.js';

export default function Dashboard() {
  const [health, setHealth] = useState(null);
  const [companies, setCompanies] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    getHealth().then(setHealth).catch((e) => setError(e.message));
    Companies.list().then(setCompanies).catch(() => {});
  }, []);

  return (
    <section>
      <h2>Dashboard</h2>
      {error && <p className="error">Health check failed: {error}</p>}
      {health && (
        <div className="card">
          <strong>Backend:</strong> {health.status}{' '}
          <span className="muted">({health.service})</span>
        </div>
      )}
      <h3>Companies</h3>
      {companies.length === 0 ? (
        <p className="muted">No companies yet — create one on the Companies page.</p>
      ) : (
        <ul className="card-list">
          {companies.map((c) => (
            <CompanyCard key={c.shortName} company={c} />
          ))}
        </ul>
      )}
    </section>
  );
}

function CompanyCard({ company }) {
  const [summary, setSummary] = useState(null);
  useEffect(() => {
    Reports.summary(company.shortName).then(setSummary).catch(() => {});
  }, [company.shortName]);
  return (
    <li className="card">
      <strong>{company.longName}</strong> <span className="muted">({company.shortName})</span>
      <div>Standard rate: {company.standardRate} / kWh</div>
      <div>Plants: {company.plantIds.length} | Substations: {company.substationIds.length} | Transformers: {company.transformerIds.length} | Customers: {company.customerAccountNumbers.length}</div>
      {summary && (
        <div className="muted">Revenue {summary.totalRevenue} | Cost {summary.totalCost} | Net {summary.netIncome}</div>
      )}
    </li>
  );
}
