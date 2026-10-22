package com.roundnet.insights.ingest.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

class AnalyzerExportValidatorTests {

	private static final List<AnalyzerExport.Player> PLAYERS = List.of(
			new AnalyzerExport.Player("player-1", "Alex"),
			new AnalyzerExport.Player("player-2", "Blake"),
			new AnalyzerExport.Player("player-3", "Casey"),
			new AnalyzerExport.Player("player-4", "Drew"));

	private static final List<List<String>> TEAMS = List.of(
			List.of("player-1", "player-2"),
			List.of("player-3", "player-4"));

	private final AnalyzerExportValidator validator = new AnalyzerExportValidator();

	// A small export with the four players above and the given points.
	private static AnalyzerExport export(Integer formatVersion, AnalyzerExport.Point... points) {
		return new AnalyzerExport(formatVersion, "2026-09-12T23:15:00.000Z", null,
				PLAYERS, TEAMS, List.of(), List.of(points));
	}

	// A point with one legal serve by the given player, won by Team 1.
	private static AnalyzerExport.Point pointServedBy(String serverId) {
		AnalyzerExport.Serve serve = new AnalyzerExport.Serve("serve-id", 1.0, serverId, null, "Right",
				"Reverse", 1, "Legal", null, null, "Returned", false, null);
		return new AnalyzerExport.Point("point-id", 1, serverId, List.of(serve), "Team 1", "Other",
				null, null, List.of());
	}

	@Test
	void acceptsValidExport() {
		ValidationResult result = validator.validate(export(3, pointServedBy("player-1")));

		assertThat(result.problems()).isEmpty();
	}

	@Test
	void acceptsV1ExportWithoutFormatVersion() {
		ValidationResult result = validator.validate(export(null, pointServedBy("player-3")));

		assertThat(result.problems()).isEmpty();
	}

	@Test
	void rejectsUnsupportedFormatVersion() {
		ValidationResult result = validator.validate(export(7, pointServedBy("player-1")));

		assertThat(result.hasErrors()).isTrue();
		assertThat(result.problems()).singleElement().satisfies(problem -> {
			assertThat(problem.severity()).isEqualTo(Severity.ERROR);
			assertThat(problem.code()).isEqualTo(ProblemCode.UNSUPPORTED_FORMAT_VERSION);
			assertThat(problem.point()).isNull();
		});
	}

	@Test
	void stopsAfterStructureError() {
		// The bad server is never reported: with an unknown format, the points can't be trusted.
		ValidationResult result = validator.validate(export(7, pointServedBy("player-9")));

		assertThat(result.problems()).extracting(Problem::code)
				.containsExactly(ProblemCode.UNSUPPORTED_FORMAT_VERSION);
	}

	@Test
	void reportsEveryPointWhoseServerIsNotOnATeam() {
		ValidationResult result = validator.validate(export(3,
				pointServedBy("player-1"),
				pointServedBy("player-9"),
				pointServedBy("player-4"),
				pointServedBy("nobody")));

		assertThat(result.hasErrors()).isTrue();
		assertThat(result.problems()).allSatisfy(problem -> {
			assertThat(problem.severity()).isEqualTo(Severity.ERROR);
			assertThat(problem.code()).isEqualTo(ProblemCode.SERVER_NOT_ON_TEAM);
		});
		assertThat(result.problems()).extracting(Problem::point).containsExactly(2, 4);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"2026-09-12_alex-blake_vs_casey-drew.json",
			"2026-09-19_alex-casey_vs_emery-finley.json",
			"2026-09-26_blake-drew_vs_emery-finley.json",
			"2026-10-03_alex-blake_vs_emery-finley.json",
			"2026-10-10_casey-drew_vs_emery-finley.json",
			"2026-10-17_alex-emery_vs_blake-casey.json",
			"2026-10-20_v1_partial_tagging.json",
	})
	void exampleExportsHaveNoErrors(String fileName) throws IOException {
		AnalyzerExport export;
		try (InputStream in = getClass().getResourceAsStream("/analyzer-exports/" + fileName)) {
			export = JsonMapper.builder().build().readValue(in, AnalyzerExport.class);
		}

		assertThat(validator.validate(export).hasErrors()).isFalse();
	}

}
