import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Auth, tokenStore } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const token = tokenStore.get();
    if (!token) { setReady(true); return; }
    Auth.me()
      .then((me) => setUser({ username: me.username, roles: me.roles || [] }))
      .catch(() => { tokenStore.clear(); setUser(null); })
      .finally(() => setReady(true));
  }, []);

  useEffect(() => {
    const onExpired = () => setUser(null);
    window.addEventListener('powergrid:auth-expired', onExpired);
    return () => window.removeEventListener('powergrid:auth-expired', onExpired);
  }, []);

  const login = useCallback(async (username, password) => {
    const resp = await Auth.login(username, password);
    tokenStore.set(resp.token);
    setUser({ username: resp.username, roles: resp.roles || [] });
    return resp;
  }, []);

  const logout = useCallback(async () => {
    try { await Auth.logout(); } catch { /* best-effort */ }
    tokenStore.clear();
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (...roles) => !!user && roles.some((r) => user.roles.includes(r)),
    [user]
  );

  return (
    <AuthContext.Provider value={{ user, ready, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
