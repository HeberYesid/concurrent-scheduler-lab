package com.proyecto.web.dto;

import java.util.List;

public record SimulationRequest(
        int processCount,
        long seed,
        double highPriorityRatio,
        long arrivalTimeMax,
        long burstTimeMin,
        long burstTimeMax,
        SchedulerConfigDto config,
        List<String> strategies
) {}
