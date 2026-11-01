package com.orthanc.orthanc_prototype.assignment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

class AssignmentRuleTest {

	private static final Condition CHIP_X_SERIES = Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("X_SERIES"));
	private static final Condition MORE_THAN_20_WAFERS =
			Condition.of(LotAttribute.WAFER_COUNT, Operator.GREATER_THAN, List.of("20"));

	@Test
	void createSetsFieldsAndIsEnabled() {
		AssignmentRule rule = AssignmentRule.create("Big X-series", 10, List.of(CHIP_X_SERIES), "ETCH-01");

		assertEquals("Big X-series", rule.getName());
		assertEquals(10, rule.getPriority());
		assertEquals(List.of(CHIP_X_SERIES), rule.getConditions());
		assertEquals("ETCH-01", rule.getTargetMachineId());
		assertTrue(rule.isEnabled());
	}

	@Test
	void matchesOnlyWhenAllConditionsMatch() {
		AssignmentRule rule = AssignmentRule.create("Big X-series", 10, List.of(CHIP_X_SERIES, MORE_THAN_20_WAFERS), "ETCH-01");

		assertTrue(rule.matches(new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25)));
		assertFalse(rule.matches(new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 5)));
		assertFalse(rule.matches(new Lot(ChipType.Z_SERIES, ProcessType.STANDARD, 25)));
	}

	@Test
	void ruleWithoutConditionsMatchesEveryLot() {
		AssignmentRule rule = AssignmentRule.create("Catch-all", 999, List.of(), "ETCH-01");

		assertTrue(rule.matches(new Lot(ChipType.Q_SERIES, ProcessType.LEGACY, 0)));
	}

	@Test
	void disabledRuleNeverMatches() {
		AssignmentRule rule = AssignmentRule.create("Catch-all", 999, List.of(), "ETCH-01");

		rule.disable();
		assertFalse(rule.matches(new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25)));

		rule.enable();
		assertTrue(rule.matches(new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25)));
	}

	@Test
	void updateReplacesDefinitionButKeepsIdentity() {
		AssignmentRule rule = AssignmentRule.create("Big X-series", 10, List.of(CHIP_X_SERIES), "ETCH-01");
		AssignmentRuleId id = rule.getId();

		rule.update("Big lots", 5, List.of(MORE_THAN_20_WAFERS), "ETCH-02");

		assertEquals(id, rule.getId());
		assertEquals("Big lots", rule.getName());
		assertEquals(5, rule.getPriority());
		assertEquals(List.of(MORE_THAN_20_WAFERS), rule.getConditions());
		assertEquals("ETCH-02", rule.getTargetMachineId());
	}

	@Test
	void rejectsInvalidDefinition() {
		assertThrows(InvalidAssignmentRuleException.class, () -> AssignmentRule.create(" ", 1, List.of(), "ETCH-01"));
		assertThrows(InvalidAssignmentRuleException.class, () -> AssignmentRule.create(null, 1, List.of(), "ETCH-01"));
		assertThrows(InvalidAssignmentRuleException.class, () -> AssignmentRule.create("R", -1, List.of(), "ETCH-01"));
		assertThrows(InvalidAssignmentRuleException.class, () -> AssignmentRule.create("R", 1, null, "ETCH-01"));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> AssignmentRule.create("R", 1, Arrays.asList((Condition) null), "ETCH-01"));
		assertThrows(InvalidAssignmentRuleException.class, () -> AssignmentRule.create("R", 1, List.of(), ""));
	}

	@Test
	void conditionsCannotBeModifiedDirectly() {
		AssignmentRule rule = AssignmentRule.create("Big X-series", 10, List.of(CHIP_X_SERIES), "ETCH-01");

		assertThrows(UnsupportedOperationException.class, () -> rule.getConditions().add(MORE_THAN_20_WAFERS));
	}

	@Test
	void rehydrateRestoresState() {
		AssignmentRuleId id = AssignmentRuleId.newId();

		AssignmentRule rule = AssignmentRule.rehydrate(id, "R", 3, false, List.of(CHIP_X_SERIES), "ETCH-02");

		assertEquals(id, rule.getId());
		assertFalse(rule.isEnabled());
		assertEquals(rule, AssignmentRule.rehydrate(id, "Other", 4, true, List.of(), "ETCH-01"));
	}

}
