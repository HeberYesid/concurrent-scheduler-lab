package com.proyecto.web.dto;

public record StrategyResult(
        String strategy,
        MetricsDto metrics,
        String error
) {}
