package com.proyecto.web;

import com.proyecto.domain.algorithm.ExponentialAgingCalculator;
import com.proyecto.domain.algorithm.LinearAgingCalculator;
import com.proyecto.domain.algorithm.PriorityCalculator;
import com.proyecto.domain.algorithm.StepAgingCalculator;
import com.proyecto.domain.model.ProcessTask;
import com.proyecto.domain.model.Result;
import com.proyecto.domain.model.SchedulerConfig;
import com.proyecto.domain.model.SchedulerError;
import com.proyecto.domain.model.SchedulerMetrics;
import com.proyecto.domain.model.SchedulerRun;
import com.proyecto.domain.service.SchedulerService;
import com.proyecto.domain.service.impl.AgingSchedulerService;
import com.proyecto.web.dto.MetricsDto;
import com.proyecto.web.dto.SchedulerConfigDto;
import com.proyecto.web.dto.SimulationRequest;
import com.proyecto.web.dto.SimulationResponse;
import com.proyecto.web.dto.StrategyResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {

    private static final Map<String, String> STRATEGIES = Map.of(
            "LINEAR", "LinearAgingCalculator — crecimiento proporcional al tiempo de espera",
            "EXPONENTIAL", "ExponentialAgingCalculator — crecimiento logaritmico (suave)",
            "STEP", "StepAgingCalculator — incrementos discretos por intervalo"
    );

    @PostMapping("/simulate")
    public SimulationResponse simulate(@RequestBody SimulationRequest request) {
        long start = System.currentTimeMillis();

        List<ProcessTask> tasks = generateProcesses(
                request.processCount(),
                request.seed(),
                request.highPriorityRatio(),
                request.arrivalTimeMax(),
                request.burstTimeMin(),
                request.burstTimeMax()
        );

        SchedulerConfig config = new SchedulerConfig(
                request.config().agingFactor(),
                request.config().agingInterval(),
                request.config().maxAcceptableWait()
        );

        List<String> strategies = request.strategies();
        if (strategies == null || strategies.isEmpty()) {
            strategies = List.of("LINEAR", "EXPONENTIAL", "STEP");
        }

        List<StrategyResult> results = new ArrayList<>();
        for (String strategy : strategies) {
            StrategyResult result = runSimulation(tasks, config, strategy);
            results.add(result);
        }

        long duration = System.currentTimeMillis() - start;
        return new SimulationResponse(duration, results);
    }

    @PostMapping("/strategies")
    public List<Map<String, String>> getStrategies() {
        return STRATEGIES.entrySet().stream()
                .map(e -> Map.of("name", e.getKey(), "description", e.getValue()))
                .toList();
    }

    private StrategyResult runSimulation(List<ProcessTask> tasks, SchedulerConfig config, String strategy) {
        try {
            PriorityCalculator calculator = switch (strategy.toUpperCase()) {
                case "LINEAR" -> new LinearAgingCalculator();
                case "EXPONENTIAL" -> new ExponentialAgingCalculator();
                case "STEP" -> new StepAgingCalculator();
                default -> throw new IllegalArgumentException("Estrategia desconocida: " + strategy);
            };

            SchedulerService scheduler = new AgingSchedulerService(calculator);
            Result<SchedulerRun, SchedulerError> result = scheduler.schedule(tasks, config);

            if (result.isOk()) {
                SchedulerMetrics m = result.getValue().metrics();
                MetricsDto dto = new MetricsDto(
                        m.throughput(), m.avgWaitTime(), m.starvationRate(),
                        m.processedCount(), m.totalElapsedTimeMs()
                );
                return new StrategyResult(strategy.toUpperCase(), dto, null);
            } else {
                return new StrategyResult(strategy.toUpperCase(), null, result.getError().getMessage());
            }
        } catch (Exception e) {
            return new StrategyResult(strategy.toUpperCase(), null, e.getMessage());
        }
    }

    private List<ProcessTask> generateProcesses(
            int n, long seed, double highPriorityRatio,
            long arrivalTimeMax, long burstTimeMin, long burstTimeMax
    ) {
        Random random = new Random(seed);
        List<ProcessTask> tasks = new ArrayList<>(n);

        for (int i = 1; i <= n; i++) {
            int priority = random.nextDouble() < highPriorityRatio
                    ? random.nextInt(10)
                    : 10 + random.nextInt(30);

            long arrivalTime = random.nextLong(arrivalTimeMax);
            long burstTime = burstTimeMin + random.nextLong(burstTimeMax - burstTimeMin + 1);

            tasks.add(new ProcessTask(i, priority, arrivalTime, burstTime));
        }
        return tasks;
    }
}
