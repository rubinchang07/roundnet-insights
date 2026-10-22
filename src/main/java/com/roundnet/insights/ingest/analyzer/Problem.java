package com.roundnet.insights.ingest.analyzer;

public record Problem(Severity severity, ProblemCode code, Integer point, Integer serve,
    Integer touch, String message
) {}