import { type FormState } from "../types";

const STRATEGY_LABELS: Record<string, string> = {
  LINEAR: "Linear",
  EXPONENTIAL: "Exponential",
  STEP: "Step",
};

interface Props {
  form: FormState;
  loading: boolean;
  onChange: (field: keyof FormState, value: number | boolean | string) => void;
  onToggleStrategy: (strategy: string) => void;
  onSubmit: () => void;
}

export default function SimulationForm({
  form,
  loading,
  onChange,
  onToggleStrategy,
  onSubmit,
}: Props) {
  return (
    <form
      className="simulation-form"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit();
      }}
    >
      <h2>Simulation Parameters</h2>

      <div className="form-grid">
        <label>
          <span>Process Count</span>
          <input
            type="number"
            min={100}
            max={100000}
            step={100}
            value={form.processCount}
            onChange={(e) => onChange("processCount", Number(e.target.value))}
          />
        </label>

        <label>
          <span>Seed</span>
          <input
            type="number"
            value={form.seed}
            onChange={(e) => onChange("seed", Number(e.target.value))}
          />
        </label>

        <label>
          <span>High Priority Ratio</span>
          <input
            type="range"
            min={0}
            max={1}
            step={0.05}
            value={form.highPriorityRatio}
            onChange={(e) =>
              onChange("highPriorityRatio", Number(e.target.value))
            }
          />
          <small>{form.highPriorityRatio}</small>
        </label>

        <label>
          <span>Max Arrival Time (ms)</span>
          <input
            type="number"
            min={100}
            max={100000}
            step={100}
            value={form.arrivalTimeMax}
            onChange={(e) => onChange("arrivalTimeMax", Number(e.target.value))}
          />
        </label>

        <label>
          <span>Min Burst Time (ms)</span>
          <input
            type="number"
            min={1}
            max={60000}
            step={1}
            value={form.burstTimeMin}
            onChange={(e) => onChange("burstTimeMin", Number(e.target.value))}
          />
        </label>

        <label>
          <span>Max Burst Time (ms)</span>
          <input
            type="number"
            min={1}
            max={60000}
            step={1}
            value={form.burstTimeMax}
            onChange={(e) => onChange("burstTimeMax", Number(e.target.value))}
          />
        </label>

        <label>
          <span>Aging Factor</span>
          <input
            type="range"
            min={0.05}
            max={1}
            step={0.05}
            value={form.agingFactor}
            onChange={(e) => onChange("agingFactor", Number(e.target.value))}
          />
          <small>{form.agingFactor}</small>
        </label>

        <label>
          <span>Aging Interval (ms)</span>
          <input
            type="number"
            min={10}
            max={5000}
            step={10}
            value={form.agingInterval}
            onChange={(e) => onChange("agingInterval", Number(e.target.value))}
          />
        </label>

        <label>
          <span>Max Acceptable Wait (ms)</span>
          <input
            type="number"
            min={100}
            max={60000}
            step={100}
            value={form.maxAcceptableWait}
            onChange={(e) =>
              onChange("maxAcceptableWait", Number(e.target.value))
            }
          />
        </label>
      </div>

      <fieldset>
        <legend>Aging Strategies to Compare</legend>
        <div className="strategy-checkboxes">
          {Object.entries(STRATEGY_LABELS).map(([key, label]) => (
            <label key={key} className="checkbox-label">
              <input
                type="checkbox"
                checked={form.strategies[key] ?? true}
                onChange={() => onToggleStrategy(key)}
              />
              {label}
            </label>
          ))}
        </div>
      </fieldset>

      <button type="submit" disabled={loading} className="run-button">
        {loading ? "Running Simulation..." : "Run Simulation"}
      </button>
    </form>
  );
}
