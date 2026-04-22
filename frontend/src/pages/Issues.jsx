import { useEffect, useState } from 'react';
import { Companies, Issues as Api, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Issues() {
  const [list, setList] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [form, setForm] = useState({
    companyShortName: '', assetType: 'PLANT', assetId: '',
    hoursRequired: '', materialsCost: '',
  });
  const [assignForm, setAssignForm] = useState({ issueId: '', employeeId: '' });
  const toast = useToast();

  const refresh = () => Api.list().then(setList).catch(() => {});
  useEffect(() => {
    Companies.list().then(setCompanies).catch(() => {});
    refresh();
  }, []);

  const report = (e) => {
    e.preventDefault();
    Api.report({
      companyShortName: form.companyShortName,
      assetType: form.assetType,
      assetId: form.assetId,
      hoursRequired: Number(form.hoursRequired),
      materialsCost: Number(form.materialsCost),
    })
      .then(() => {
        toast.success('Issue reported.');
        setForm({ companyShortName: '', assetType: 'PLANT', assetId: '', hoursRequired: '', materialsCost: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  const assign = (e) => {
    e.preventDefault();
    Api.assign(Number(assignForm.issueId), assignForm.employeeId)
      .then(() => {
        toast.success('Assigned.');
        setAssignForm({ issueId: '', employeeId: '' });
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  const resolve = (id) => {
    Api.resolve(id)
      .then(() => {
        toast.success(`Issue ${id} resolved.`);
        refresh();
      })
      .catch((err) => toast.error(extractError(err)));
  };

  return (
    <section>
      <h2>Equipment issues</h2>
      <div className="grid">
        <form className="form stacked" onSubmit={report}>
          <h4>Report issue</h4>
          <select required value={form.companyShortName}
            onChange={(e) => setForm({ ...form, companyShortName: e.target.value })}>
            <option value="">Company</option>
            {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
          </select>
          <select value={form.assetType}
            onChange={(e) => setForm({ ...form, assetType: e.target.value })}>
            <option value="PLANT">PLANT</option>
            <option value="SUBSTATION">SUBSTATION</option>
            <option value="TRANSFORMER">TRANSFORMER</option>
          </select>
          <input required placeholder="Asset ID" value={form.assetId}
            onChange={(e) => setForm({ ...form, assetId: e.target.value })} />
          <input required type="number" step="0.1" placeholder="Hours required"
            value={form.hoursRequired}
            onChange={(e) => setForm({ ...form, hoursRequired: e.target.value })} />
          <input required type="number" step="0.01" placeholder="Materials cost"
            value={form.materialsCost}
            onChange={(e) => setForm({ ...form, materialsCost: e.target.value })} />
          <button type="submit">Report</button>
        </form>
        <form className="form stacked" onSubmit={assign}>
          <h4>Assign issue to employee</h4>
          <input required type="number" placeholder="Issue ID" value={assignForm.issueId}
            onChange={(e) => setAssignForm({ ...assignForm, issueId: e.target.value })} />
          <input required placeholder="Employee ID" value={assignForm.employeeId}
            onChange={(e) => setAssignForm({ ...assignForm, employeeId: e.target.value })} />
          <button type="submit">Assign</button>
        </form>
      </div>

      <h3>All issues</h3>
      <ul className="card-list">
        {list.map((i) => (
          <li key={i.issueId} className="card">
            <strong>#{i.issueId}</strong> {i.assetType} {i.assetId} ·
            {' '}<span className="muted">{i.status}</span>
            <div className="muted">
              {i.companyShortName} · {i.hoursRequired}h · materials ${i.materialsCost}
              {i.assignedEmployeeId && <> · assigned: {i.assignedEmployeeId}</>}
              {i.resolutionCost && <> · cost: ${i.resolutionCost}</>}
            </div>
            {i.status === 'ASSIGNED' && (
              <button onClick={() => resolve(i.issueId)}>Resolve</button>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
