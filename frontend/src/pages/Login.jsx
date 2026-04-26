import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { extractError } from '../api/client.js';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const { login } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();
  const redirectTo = loc.state?.from || '/';

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError(null);
    try {
      await login(username, password);
      nav(redirectTo, { replace: true });
    } catch (err) {
      setError(extractError(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-shell">
      <form className="login-card" onSubmit={submit}>
        <h1>EPD Console</h1>
        <p className="subtitle">CS6310 // Group 30 // Phase 3</p>
        <label>
          Operator ID
          <input autoFocus autoComplete="username" required
            value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
        <label>
          Passphrase
          <input type="password" autoComplete="current-password" required
            value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        {error && (
          <div className="login-error">
            <strong>{error.code}</strong>
            <div>{error.message}</div>
            {error.remediation && <div className="remediation">{error.remediation}</div>}
          </div>
        )}
        <button type="submit" disabled={busy}>
          {busy ? 'Authenticating…' : 'Authorize'}
        </button>
        <p className="hint">Bootstrap credentials &mdash; <code>admin</code> / <code>admin123</code></p>
      </form>
    </div>
  );
}
