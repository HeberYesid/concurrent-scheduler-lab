export interface SchedulerConfigDto {
  agingFactor: number;
  agingInterval: number;
  maxAcceptableWait: number;
}

export interface MetricsDto {
  throughput: number;
  avgWaitTime: number;
  starvationRate: number;
  processedCount: number;
  totalElapsedTimeMs: number;
}

export interface StrategyResult {
  strategy: string;
  metrics: MetricsDto | null;
  error: string | null;
}

export interface SimulationResponse {
  simulationTimeMs: number;
  results: StrategyResult[];
}

export interface SimulationRequest {
  processCount: number;
  seed: number;
  highPriorityRatio: number;
  arrivalTimeMax: number;
  burstTimeMin: number;
  burstTimeMax: number;
  config: SchedulerConfigDto;
  strategies: string[];
}

export interface FormState {
  processCount: number;
  seed: number;
  highPriorityRatio: number;
  arrivalTimeMax: number;
  burstTimeMin: number;
  burstTimeMax: number;
  agingFactor: number;
  agingInterval: number;
  maxAcceptableWait: number;
  strategies: Record<string, boolean>;
}
