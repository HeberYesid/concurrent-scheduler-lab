package com.proyecto.web.dto;

import java.util.List;

public record SimulationResponse(
        long simulationTimeMs,
        List<StrategyResult> results
) {}
