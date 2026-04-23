import { useEffect, useState } from 'react';
import { Companies, Infrastructure as Api, ENERGY_SOURCES, extractError } from '../api/client.js';
import { useToast } from '../components/Toast.jsx';

export default function Infrastructure() {
  const [companies, setCompanies] = useState([]);
  const [plants, setPlants] = useState([]);
  const [subs, setSubs] = useState([]);
  const [trs, setTrs] = useState([]);
  const toast = useToast();

  const refresh = () => {
    Api.listPlants().then(setPlants).catch(() => {});
    Api.listSubstations().then(setSubs).catch(() => {});
    Api.listTransformers().then(setTrs).catch(() => {});
  };
  useEffect(() => {
    Companies.list().then(setCompanies).catch(() => {});
    refresh();
  }, []);

  return (
    <section>
      <h2>Infrastructure</h2>
      <div className="grid">
        <AssetForm label="Plant" companies={companies}
          idField="plantId"
          fields={[
            { name: 'buildCost', label: 'Build cost', type: 'number', step: '0.01' },
            { name: 'generationCostPerKWh', label: 'Generation cost / kWh', type: 'number', step: '0.0001' },
            { name: 'energySource', label: 'Energy source', type: 'select', options: ENERGY_SOURCES, default: 'NATURAL_GAS' },
          ]}
          onSubmit={Api.createPlant} afterSubmit={refresh} toast={toast} />
        <AssetForm label="Substation" companies={companies}
          idField="substationId"
          fields={[
            { name: 'buildCost', label: 'Build cost', type: 'number', step: '0.01' },
            { name: 'maintenanceCostPerCycle', label: 'Maintenance / cycle', type: 'number', step: '0.01' },
          ]}
          onSubmit={Api.createSubstation} afterSubmit={refresh} toast={toast} />
        <AssetForm label="Transformer" companies={companies}
          idField="transformerId"
          fields={[
            { name: 'installCost', label: 'Install cost', type: 'number', step: '0.01' },
            { name: 'maintenanceCostPerCycle', label: 'Maintenance / cycle', type: 'number', step: '0.01' },
          ]}
          onSubmit={Api.createTransformer} afterSubmit={refresh} toast={toast} />
      </div>

      <h3>Connections</h3>
      <div className="grid">
        <ConnectForm label="Plant → Substation"
          onSubmit={(s, t) => Api.connectPlantSubstation(s, t)}
          sourceLabel="Plant ID" targetLabel="Substation ID"
          afterSubmit={refresh} toast={toast} />
        <ConnectForm label="Substation → Transformer"
          onSubmit={(s, t) => Api.connectSubstationTransformer(s, t)}
          sourceLabel="Substation ID" targetLabel="Transformer ID"
          afterSubmit={refresh} toast={toast} />
      </div>

      <h3>Inventory</h3>
      <div className="grid">
        <InventoryList title={`Plants (${plants.length})`} items={plants} idKey="plantId"
          render={(p) => `${p.plantId} · ${p.companyShortName} · ${p.energySource ?? '?'}${p.renewable ? ' ♻' : ''} · (${p.location.x},${p.location.y}) · ${p.substationIds.length}/${p.maxSubstations} subs`} />
        <InventoryList title={`Substations (${subs.length})`} items={subs} idKey="substationId"
          render={(s) => `${s.substationId} · ${s.companyShortName} · src=${s.sourcePlantId ?? '-'} · ${s.transformerIds.length}/${s.maxTransformers} xfmrs`} />
        <InventoryList title={`Transformers (${trs.length})`} items={trs} idKey="transformerId"
          render={(t) => `${t.transformerId} · ${t.companyShortName} · src=${t.sourceSubstationId ?? '-'} · ${t.customerAccountNumbers.length}/${t.maxCustomers} cust`} />
      </div>
    </section>
  );
}

function AssetForm({ label, companies, idField, fields, onSubmit, afterSubmit, toast }) {
  const [state, setState] = useState({
    companyShortName: '', [idField]: '', x: '', y: '',
    ...Object.fromEntries(fields.map((f) => [f.name, f.default ?? ''])),
  });
  const submit = (e) => {
    e.preventDefault();
    const payload = {
      companyShortName: state.companyShortName,
      [idField]: state[idField],
      location: { x: Number(state.x), y: Number(state.y) },
      ...Object.fromEntries(fields.map((f) => [
        f.name,
        f.type === 'select' ? state[f.name] : Number(state[f.name]),
      ])),
    };
    onSubmit(payload)
      .then(() => {
        toast.success(`${label} created.`);
        afterSubmit();
      })
      .catch((err) => toast.error(extractError(err)));
  };
  return (
    <form className="form stacked" onSubmit={submit}>
      <h4>Add {label}</h4>
      <select required value={state.companyShortName}
        onChange={(e) => setState({ ...state, companyShortName: e.target.value })}>
        <option value="">Select company</option>
        {companies.map((c) => <option key={c.shortName} value={c.shortName}>{c.shortName}</option>)}
      </select>
      <input required placeholder={`${label} ID`} value={state[idField]}
        onChange={(e) => setState({ ...state, [idField]: e.target.value })} />
      <div className="row">
        <input required type="number" placeholder="x" value={state.x}
          onChange={(e) => setState({ ...state, x: e.target.value })} />
        <input required type="number" placeholder="y" value={state.y}
          onChange={(e) => setState({ ...state, y: e.target.value })} />
      </div>
      {fields.map((f) => f.type === 'select' ? (
        <label key={f.name} className="stacked">
          <span className="muted small">{f.label}</span>
          <select required value={state[f.name]}
            onChange={(e) => setState({ ...state, [f.name]: e.target.value })}>
            {f.options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </label>
      ) : (
        <input key={f.name} required type={f.type} step={f.step} placeholder={f.label}
          value={state[f.name]}
          onChange={(e) => setState({ ...state, [f.name]: e.target.value })} />
      ))}
      <button type="submit">Create</button>
    </form>
  );
}

function ConnectForm({ label, onSubmit, sourceLabel, targetLabel, afterSubmit, toast }) {
  const [source, setSource] = useState('');
  const [target, setTarget] = useState('');
  const submit = (e) => {
    e.preventDefault();
    onSubmit(source, target)
      .then(() => {
        toast.success(`${label}: connected.`);
        setSource('');
        setTarget('');
        afterSubmit();
      })
      .catch((err) => toast.error(extractError(err)));
  };
  return (
    <form className="form stacked" onSubmit={submit}>
      <h4>{label}</h4>
      <input required placeholder={sourceLabel} value={source} onChange={(e) => setSource(e.target.value)} />
      <input required placeholder={targetLabel} value={target} onChange={(e) => setTarget(e.target.value)} />
      <button type="submit">Connect</button>
    </form>
  );
}

function InventoryList({ title, items, idKey, render }) {
  return (
    <div className="card">
      <h4>{title}</h4>
      {items.length === 0 ? <p className="muted">None.</p> : (
        <ul>
          {items.map((item) => <li key={item[idKey]}>{render(item)}</li>)}
        </ul>
      )}
    </div>
  );
}
