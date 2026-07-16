package com.proyecto.web.dto;

public record MetricsDto(
        double throughput,
        double avgWaitTime,
        double starvationRate,
        int processedCount,
        long totalElapsedTimeMs
) {}
