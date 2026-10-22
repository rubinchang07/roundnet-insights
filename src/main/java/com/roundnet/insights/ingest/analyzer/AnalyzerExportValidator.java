package com.roundnet.insights.ingest.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Checks an Analyzer export before anything is saved. Reads only the export, never the database. */
public class AnalyzerExportValidator {

    public ValidationResult validate(AnalyzerExport export) {
        List<Problem> problems = new ArrayList<>();
        Integer formatVersion = export.formatVersion();

        if (formatVersion != null && !List.of(1, 2, 3).contains(formatVersion)) {
            problems.add(new Problem(Severity.ERROR, ProblemCode.UNSUPPORTED_FORMAT_VERSION, null, null, null,
                "unsupported formatVersion " + formatVersion));
            return new ValidationResult(problems);
        }

        Set<String> playerIds = new HashSet<>();
        for (List<String> team : export.teams()) {
            playerIds.addAll(team);
        }

        for (int i = 0; i < export.points().size(); i++) {
            if (!playerIds.contains(export.points().get(i).serverId())) {
                problems.add(new Problem(Severity.ERROR, ProblemCode.SERVER_NOT_ON_TEAM, i + 1, null, null, "server " + export.points().get(i).serverId() + " is not on either team."));
            }
        }
        return new ValidationResult(problems);
    }
}
