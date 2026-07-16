import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import type { SimulationResponse } from "../types";

interface Props {
  data: SimulationResponse;
}

export default function ResultsDashboard({ data }: Props) {
  if (!data || !data.results || data.results.length === 0) {
    return null;
  }

  const results = data.results;

  const chartData = results
    .filter((r) => r.metrics)
    .map((r) => ({
      strategy: r.strategy,
      "Throughput (proc/s)": Math.round(r.metrics!.throughput * 100) / 100,
      "Avg Wait (ms)": Math.round(r.metrics!.avgWaitTime),
      "Starvation Rate (%)":
        Math.round(r.metrics!.starvationRate * 10000) / 100,
    }));

  return (
    <div className="dashboard">
      <h2>
        Simulation Results{" "}
        <span className="duration">
          ({data.simulationTimeMs}ms wall-clock)
        </span>
      </h2>

      <div className="metrics-table-wrapper">
        <table className="metrics-table">
          <thead>
            <tr>
              <th>Strategy</th>
              <th>Throughput (proc/s)</th>
              <th>Avg Wait (ms)</th>
              <th>Starvation Rate</th>
              <th>Processed</th>
              <th>Total Time (ms)</th>
            </tr>
          </thead>
          <tbody>
            {results.map((r) => (
              <tr key={r.strategy}>
                <td className="strategy-cell">
                  <span className={`badge badge-${r.strategy.toLowerCase()}`}>
                    {r.strategy}
                  </span>
                </td>
                {r.metrics ? (
                  <>
                    <td>{r.metrics.throughput.toFixed(2)}</td>
                    <td>{r.metrics.avgWaitTime.toFixed(1)}</td>
                    <td>{(r.metrics.starvationRate * 100).toFixed(2)}%</td>
                    <td>{r.metrics.processedCount}</td>
                    <td>{r.metrics.totalElapsedTimeMs}</td>
                  </>
                ) : (
                  <td colSpan={5} className="error-cell">
                    {r.error}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {chartData.length > 0 && (
        <>
          <h3>Throughput Comparison</h3>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#45475a" />
                <XAxis dataKey="strategy" stroke="#a6adc8" />
                <YAxis stroke="#a6adc8" />
                <Tooltip
                  contentStyle={{
                    background: "#1e1e2e",
                    border: "1px solid #45475a",
                    borderRadius: "8px",
                    color: "#cdd6f4",
                  }}
                />
                <Legend />
                <Bar
                  dataKey="Throughput (proc/s)"
                  fill="#89b4fa"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <h3>Average Wait Time (ms)</h3>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#45475a" />
                <XAxis dataKey="strategy" stroke="#a6adc8" />
                <YAxis stroke="#a6adc8" />
                <Tooltip
                  contentStyle={{
                    background: "#1e1e2e",
                    border: "1px solid #45475a",
                    borderRadius: "8px",
                    color: "#cdd6f4",
                  }}
                />
                <Legend />
                <Bar
                  dataKey="Avg Wait (ms)"
                  fill="#a6e3a1"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <h3>Starvation Rate (%)</h3>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#45475a" />
                <XAxis dataKey="strategy" stroke="#a6adc8" />
                <YAxis stroke="#a6adc8" />
                <Tooltip
                  contentStyle={{
                    background: "#1e1e2e",
                    border: "1px solid #45475a",
                    borderRadius: "8px",
                    color: "#cdd6f4",
                  }}
                />
                <Legend />
                <Bar
                  dataKey="Starvation Rate (%)"
                  fill="#f38ba8"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
    </div>
  );
}
