import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

export const http = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

export function extractError(err) {
  const data = err?.response?.data;
  if (data && data.code) {
    return {
      code: data.code,
      message: data.message || 'Request failed.',
      field: data.field || null,
      remediation: data.remediation || null,
    };
  }
  return { code: 'NETWORK_ERROR', message: err?.message || 'Network error.' };
}

export async function getHealth() {
  const { data } = await http.get('/health');
  return data;
}

export const Companies = {
  list: () => http.get('/companies').then((r) => r.data),
  get: (shortName) => http.get(`/companies/${shortName}`).then((r) => r.data),
  create: (payload) => http.post('/companies', payload).then((r) => r.data),
  updateRate: (shortName, standardRate) =>
    http.put(`/companies/${shortName}/standard-rate`, { standardRate }).then((r) => r.data),
};

export const Infrastructure = {
  listPlants: () => http.get('/plants').then((r) => r.data),
  createPlant: (p) => http.post('/plants', p).then((r) => r.data),
  listSubstations: () => http.get('/substations').then((r) => r.data),
  createSubstation: (p) => http.post('/substations', p).then((r) => r.data),
  listTransformers: () => http.get('/transformers').then((r) => r.data),
  createTransformer: (p) => http.post('/transformers', p).then((r) => r.data),
  connectPlantSubstation: (sourceId, targetId) =>
    http.post('/connections/plant-substation', { sourceId, targetId }).then((r) => r.data),
  connectSubstationTransformer: (sourceId, targetId) =>
    http.post('/connections/substation-transformer', { sourceId, targetId }).then((r) => r.data),
};

export const Customers = {
  list: () => http.get('/customers').then((r) => r.data),
  get: (accountNumber) => http.get(`/customers/${accountNumber}`).then((r) => r.data),
  create: (p) => http.post('/customers', p).then((r) => r.data),
  connect: (accountNumber, transformerId) =>
    http.post('/customers/connect', { accountNumber, transformerId }).then((r) => r.data),
};

export const Employees = {
  list: () => http.get('/employees').then((r) => r.data),
  create: (p) => http.post('/employees', p).then((r) => r.data),
};

export const Issues = {
  list: () => http.get('/issues').then((r) => r.data),
  get: (id) => http.get(`/issues/${id}`).then((r) => r.data),
  report: (p) => http.post('/issues', p).then((r) => r.data),
  assign: (id, employeeId) => http.post(`/issues/${id}/assign`, { employeeId }).then((r) => r.data),
  resolve: (id) => http.post(`/issues/${id}/resolve`).then((r) => r.data),
};

export const RatePlans = {
  list: () => http.get('/rate-plans').then((r) => r.data),
  create: (p) => http.post('/rate-plans', p).then((r) => r.data),
};

export const Billing = {
  recordUsage: (p) => http.post('/billing/usage', p).then((r) => r.data),
  recordProduction: (p) => http.post('/billing/production', p).then((r) => r.data),
  run: (p) => http.post('/billing/run', p).then((r) => r.data),
};

export const Reports = {
  summary: (shortName, from, to) => {
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    return http.get(`/reports/companies/${shortName}/summary`, { params }).then((r) => r.data);
  },
  ledger: (shortName) => http.get(`/reports/companies/${shortName}/ledger`).then((r) => r.data),
};
