package com.proyecto.web.dto;

public record SchedulerConfigDto(
        double agingFactor,
        long agingInterval,
        long maxAcceptableWait
) {}
