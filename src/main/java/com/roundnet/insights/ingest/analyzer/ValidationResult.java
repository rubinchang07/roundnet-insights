package com.roundnet.insights.ingest.analyzer;

import java.util.List;

public record ValidationResult(List<Problem> problems) {
    public boolean hasErrors() {
        return problems.stream().anyMatch(a -> a.severity() == Severity.ERROR);
    }

    public List<Problem> warnings() {
        return problems.stream().filter(p -> p.severity() == Severity.WARNING).toList();
    }
}