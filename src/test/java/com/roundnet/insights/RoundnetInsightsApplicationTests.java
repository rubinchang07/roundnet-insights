package com.roundnet.insights;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RoundnetInsightsApplicationTests {

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void contextLoads() {
	}

	@Test
	void baselineMigrationCreatesSchema() {
		Integer tables = jdbc.queryForObject("""
				SELECT count(*) FROM information_schema.tables
				WHERE table_schema = 'roundnet'
				  AND table_name IN ('match', 'game', 'point', 'serve_attempt', 'rally_event', 'import_batch')
				""", Integer.class);
		assertThat(tables).isEqualTo(6);
	}

}
