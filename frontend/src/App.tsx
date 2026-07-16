import { useState, useCallback } from "react";
import SimulationForm from "./components/SimulationForm";
import ResultsDashboard from "./components/ResultsDashboard";
import { runSimulation } from "./api";
import type { FormState, SimulationResponse } from "./types";
import "./App.css";
import "./components/SimulationForm.css";
import "./components/ResultsDashboard.css";

const DEFAULT_FORM: FormState = {
  processCount: 5000,
  seed: 42,
  highPriorityRatio: 0.3,
  arrivalTimeMax: 10000,
  burstTimeMin: 10,
  burstTimeMax: 200,
  agingFactor: 0.5,
  agingInterval: 100,
  maxAcceptableWait: 3000,
  strategies: { LINEAR: true, EXPONENTIAL: true, STEP: true },
};

function App() {
  const [form, setForm] = useState<FormState>(DEFAULT_FORM);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<SimulationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleChange = useCallback(
    (field: keyof FormState, value: number | boolean | string) => {
      setForm((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const handleToggleStrategy = useCallback((strategy: string) => {
    setForm((prev) => ({
      ...prev,
      strategies: {
        ...prev.strategies,
        [strategy]: !prev.strategies[strategy],
      },
    }));
  }, []);

  const handleSubmit = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const selectedStrategies = Object.entries(form.strategies)
        .filter(([, v]) => v)
        .map(([k]) => k);

      if (selectedStrategies.length === 0) {
        setError("Select at least one strategy");
        setLoading(false);
        return;
      }

      const response = await runSimulation({
        processCount: form.processCount,
        seed: form.seed,
        highPriorityRatio: form.highPriorityRatio,
        arrivalTimeMax: form.arrivalTimeMax,
        burstTimeMin: form.burstTimeMin,
        burstTimeMax: form.burstTimeMax,
        config: {
          agingFactor: form.agingFactor,
          agingInterval: form.agingInterval,
          maxAcceptableWait: form.maxAcceptableWait,
        },
        strategies: selectedStrategies,
      });
      setResult(response);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setLoading(false);
    }
  }, [form]);

  return (
    <div className="app">
      <header className="header">
        <h1>Concurrent Scheduler Lab</h1>
        <p>
          Priority-based process scheduler with dynamic aging — compare Linear,
          Exponential, and Step strategies
        </p>
      </header>

      <main className="main">
        <SimulationForm
          form={form}
          loading={loading}
          onChange={handleChange}
          onToggleStrategy={handleToggleStrategy}
          onSubmit={handleSubmit}
        />

        {loading && (
          <div className="loading">
            <div className="spinner" />
            <p>Running simulation... this may take a few seconds</p>
          </div>
        )}

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {result && !loading && <ResultsDashboard data={result} />}
      </main>
    </div>
  );
}

export default App;
