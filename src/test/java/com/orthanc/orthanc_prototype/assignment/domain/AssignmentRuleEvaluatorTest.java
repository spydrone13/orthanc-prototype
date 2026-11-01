package com.orthanc.orthanc_prototype.assignment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

class AssignmentRuleEvaluatorTest {

	private final AssignmentRuleEvaluator evaluator = new AssignmentRuleEvaluator();

	private final Lot lot = new Lot("A100", "CMOS-7nm", 25);

	@Test
	void picksMatchingRuleWithLowestPriorityRegardlessOfInputOrder() {
		AssignmentRule catchAll = AssignmentRule.create("Catch-all", 100, List.of(), "ETCH-01");
		AssignmentRule a100 = AssignmentRule.create("A100", 10,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("A100"))), "ETCH-02");

		assertEquals(a100, evaluator.selectRuleFor(lot, List.of(catchAll, a100)).orElseThrow());
	}

	@Test
	void skipsRulesThatDoNotMatch() {
		AssignmentRule b200 = AssignmentRule.create("B200", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("B200"))), "ETCH-02");
		AssignmentRule catchAll = AssignmentRule.create("Catch-all", 100, List.of(), "ETCH-01");

		assertEquals(catchAll, evaluator.selectRuleFor(lot, List.of(b200, catchAll)).orElseThrow());
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
		AssignmentRule b200 = AssignmentRule.create("B200", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("B200"))), "ETCH-02");

		assertTrue(evaluator.selectRuleFor(lot, List.of(b200)).isEmpty());
		assertTrue(evaluator.selectRuleFor(lot, List.of()).isEmpty());
	}

}
