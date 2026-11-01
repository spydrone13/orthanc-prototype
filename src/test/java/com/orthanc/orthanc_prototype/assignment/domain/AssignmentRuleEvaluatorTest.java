package com.orthanc.orthanc_prototype.assignment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

class AssignmentRuleEvaluatorTest {

	private final AssignmentRuleEvaluator evaluator = new AssignmentRuleEvaluator();

	private final Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

	@Test
	void picksMatchingRuleWithLowestPriorityRegardlessOfInputOrder() {
		AssignmentRule catchAll = AssignmentRule.create("Catch-all", 100, List.of(), "ETCH-01");
		AssignmentRule xSeries = AssignmentRule.create("X-series", 10,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("X_SERIES"))), "ETCH-02");

		assertEquals(xSeries, evaluator.selectRuleFor(lot, List.of(catchAll, xSeries)).orElseThrow());
	}

	@Test
	void skipsRulesThatDoNotMatch() {
		AssignmentRule zSeries = AssignmentRule.create("Z-series", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("Z_SERIES"))), "ETCH-02");
		AssignmentRule catchAll = AssignmentRule.create("Catch-all", 100, List.of(), "ETCH-01");

		assertEquals(catchAll, evaluator.selectRuleFor(lot, List.of(zSeries, catchAll)).orElseThrow());
	}

	@Test
	void skipsDisabledRules() {
		AssignmentRule disabled = AssignmentRule.create("Disabled", 1, List.of(), "ETCH-02");
		disabled.disable();
		AssignmentRule enabled = AssignmentRule.create("Enabled", 2, List.of(), "ETCH-01");

		assertEquals(enabled, evaluator.selectRuleFor(lot, List.of(disabled, enabled)).orElseThrow());
	}

	@Test
	void returnsEmptyWhenNoRuleMatches() {
		AssignmentRule zSeries = AssignmentRule.create("Z-series", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("Z_SERIES"))), "ETCH-02");

		assertTrue(evaluator.selectRuleFor(lot, List.of(zSeries)).isEmpty());
		assertTrue(evaluator.selectRuleFor(lot, List.of()).isEmpty());
	}

}
