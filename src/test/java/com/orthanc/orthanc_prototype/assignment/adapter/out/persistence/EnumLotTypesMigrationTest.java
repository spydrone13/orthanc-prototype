package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * V2 deletes rules that can't be represented once chip and process type became enums.
 */
class EnumLotTypesMigrationTest {

	private final DriverManagerDataSource dataSource =
			new DriverManagerDataSource("jdbc:h2:mem:v2-migration-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");

	private final JdbcClient jdbc = JdbcClient.create(dataSource);

	@Test
	void deletesRulesWithUnknownValuesOrRemovedOperatorsAndKeepsValidOnes() {
		flyway("1").migrate();
		UUID valid = insertRule(1, "CHIP_TYPE", "IN", "X_SERIES", "Q_SERIES");
		insertCondition(valid, 1, "WAFER_COUNT", "GREATER_THAN", "20");
		UUID numericOnly = insertRule(2, "WAFER_COUNT", "LESS_THAN_OR_EQUAL", "100");
		UUID unknownChip = insertRule(3, "CHIP_TYPE", "EQUALS", "A100");
		insertCondition(unknownChip, 1, "WAFER_COUNT", "GREATER_THAN", "20");
		insertRule(4, "PROCESS_TYPE", "IN", "STANDARD", "CMOS-7nm");
		insertRule(5, "PROCESS_TYPE", "MATCHES", "STANDARD");
		insertRule(6, "CHIP_TYPE", "STARTS_WITH", "X");

		flyway(null).migrate();

		assertEquals(List.of(valid, numericOnly),
				jdbc.sql("select id from assignment_rule order by priority").query(UUID.class).list());
		assertEquals(3, count("assignment_rule_condition"));
		assertEquals(4, count("assignment_rule_condition_operand"));
	}

	private Flyway flyway(String target) {
		var configuration = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration");
		if (target != null) {
			configuration.target(target);
		}
		return configuration.load();
	}

	private UUID insertRule(int priority, String attribute, String operator, String... operands) {
		UUID id = UUID.randomUUID();
		jdbc.sql("insert into assignment_rule (id, name, priority, enabled, target_machine_id, version) "
				+ "values (?, ?, ?, true, 'ETCH-01', 0)")
			.params(id, "Rule " + priority, priority)
			.update();
		insertCondition(id, 0, attribute, operator, operands);
		return id;
	}

	private void insertCondition(UUID ruleId, int index, String attribute, String operator, String... operands) {
		jdbc.sql("insert into assignment_rule_condition (rule_id, condition_index, attribute, operator) values (?, ?, ?, ?)")
			.params(ruleId, index, attribute, operator)
			.update();
		long conditionId = jdbc.sql("select max(id) from assignment_rule_condition").query(Long.class).single();
		for (int i = 0; i < operands.length; i++) {
			jdbc.sql("insert into assignment_rule_condition_operand (condition_id, operand_index, operand_value) "
					+ "values (?, ?, ?)")
				.params(conditionId, i, operands[i])
				.update();
		}
	}

	private int count(String table) {
		return jdbc.sql("select count(*) from " + table).query(Integer.class).single();
	}

}
