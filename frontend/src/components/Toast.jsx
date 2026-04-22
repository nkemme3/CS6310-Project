import { createContext, useCallback, useContext, useState } from 'react';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const push = useCallback((toast) => {
    const id = Math.random().toString(36).slice(2);
    setToasts((curr) => [...curr, { id, ...toast }]);
    setTimeout(() => {
      setToasts((curr) => curr.filter((t) => t.id !== id));
    }, 6000);
  }, []);

  const error = useCallback((err) => {
    push({ kind: 'error', title: err.code || 'Error', message: err.message, remediation: err.remediation });
  }, [push]);

  const success = useCallback((message) => {
    push({ kind: 'success', title: 'OK', message });
  }, [push]);

  return (
    <ToastContext.Provider value={{ push, error, success }}>
      {children}
      <div className="toasts">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.kind}`}>
            <strong>{t.title}</strong>
            <div>{t.message}</div>
            {t.remediation && <div className="remediation">{t.remediation}</div>}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used inside ToastProvider');
  return ctx;
}
