package com.roundnet.insights.ingest.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.json.JsonMapper;

/** Reads the example exports (copied from roundnet-analyzer/db/examples) into {@link AnalyzerExport}. */
class AnalyzerExportParsingTests {

	private final JsonMapper mapper = JsonMapper.builder().build();

	private AnalyzerExport read(String fileName) throws IOException {
		try (InputStream in = getClass().getResourceAsStream("/analyzer-exports/" + fileName)) {
			return mapper.readValue(in, AnalyzerExport.class);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"2026-09-12_alex-blake_vs_casey-drew.json, 105",
			"2026-09-19_alex-casey_vs_emery-finley.json, 38",
			"2026-09-26_blake-drew_vs_emery-finley.json, 68",
			"2026-10-03_alex-blake_vs_emery-finley.json, 70",
			"2026-10-10_casey-drew_vs_emery-finley.json, 44",
			"2026-10-17_alex-emery_vs_blake-casey.json, 110",
	})
	void readsV3Export(String fileName, int expectedPoints) throws IOException {
		AnalyzerExport export = read(fileName);

		assertThat(export.formatVersion()).isEqualTo(3);
		assertThat(export.match()).isNotNull();
		assertThat(export.players()).hasSize(4);
		assertThat(export.teams()).hasSize(2).allSatisfy(team -> assertThat(team).hasSize(2));
		assertThat(export.points()).hasSize(expectedPoints).allSatisfy(point -> {
			assertThat(point.serves()).isNotEmpty();
			assertThat(point.rally()).isNotNull();
		});
	}

	@Test
	void readsEveryFieldOfAPoint() throws IOException {
		AnalyzerExport export = read("2026-09-12_alex-blake_vs_casey-drew.json");

		assertThat(export.match().id()).isEqualTo("f1ac2f4d-83e6-163d-294b-e65ebd65b273");
		assertThat(export.match().playedOn()).isEqualTo("2026-09-12");
		assertThat(export.match().pointsTo()).isEqualTo(21);
		assertThat(export.match().hardCap()).isNull();
		assertThat(export.players().getFirst()).isEqualTo(new AnalyzerExport.Player("player-1", "Alex"));

		AnalyzerExport.Point point = export.points().getFirst();
		assertThat(point.id()).isEqualTo("1919e93a-d117-45ad-4988-93101c593af5");
		assertThat(point.gameNumber()).isEqualTo(1);
		assertThat(point.serverId()).isEqualTo("player-1");
		assertThat(point.winningTeam()).isEqualTo("Team 1");
		assertThat(point.outcome()).isEqualTo("Offensive Error");
		assertThat(point.responsiblePlayerId()).isEqualTo("player-4");
		assertThat(point.violationType()).isNull();

		AnalyzerExport.Serve serve = point.serves().getFirst();
		assertThat(serve.timestamp()).isEqualTo(3.0);
		assertThat(serve.receiverId()).isEqualTo("player-3");
		assertThat(serve.serveAttempt()).isEqualTo(1);
		assertThat(serve.legality()).isEqualTo("Legal");
		assertThat(serve.faultType()).isNull();
		assertThat(serve.replayed()).isFalse();

		assertThat(point.rally()).extracting(AnalyzerExport.Touch::type).containsExactly("Set", "Hit");
		assertThat(point.rally().get(1).timestamp()).isEqualTo(8.41);
	}

	@Test
	void readsV1ExportWithMissingFieldsAsNull() throws IOException {
		AnalyzerExport export = read("2026-10-20_v1_partial_tagging.json");

		assertThat(export.formatVersion()).isNull();
		assertThat(export.match()).isNull();
		assertThat(export.points()).hasSize(8).allSatisfy(point -> {
			assertThat(point.gameNumber()).isNull();
			assertThat(point.rally()).isNull();
		});
	}

}
