import { useEffect, useState } from 'react';
import { getHealth } from './api/client.js';

export default function App() {
  const [health, setHealth] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getHealth()
      .then(setHealth)
      .catch((e) => setError(e.message));
  }, []);

  return (
    <main className="app">
      <header>
        <h1>Power Grid</h1>
        <p className="subtitle">CS6310 Group 30 &mdash; Spring 2026</p>
      </header>
      <section>
        <h2>Backend health</h2>
        {error && <p className="error">Error: {error}</p>}
        {!error && !health && <p>Checking...</p>}
        {health && (
          <pre className="health">{JSON.stringify(health, null, 2)}</pre>
        )}
      </section>
    </main>
  );
}
