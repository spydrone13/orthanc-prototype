package com.orthanc.orthanc_prototype.assignment.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase.AssignmentOutcome;
import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase.AutoAssignLotCommand;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleEvaluator;
import com.orthanc.orthanc_prototype.assignment.domain.Condition;
import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.InMemoryMachineRepository;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.MachineSeedProperties;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.MachineSeedProperties.MachineSeed;

class LotAutoAssignmentServiceTest {

	private final InMemoryAssignmentRuleRepository rules = new InMemoryAssignmentRuleRepository();

	private final InMemoryMachineRepository machines = new InMemoryMachineRepository(
			new MachineSeedProperties(List.of(new MachineSeed("ETCH-01", "Etcher 1"), new MachineSeed("ETCH-02", "Etcher 2"))));

	private final LotAutoAssignmentService service =
			new LotAutoAssignmentService(rules, machines, new AssignmentRuleEvaluator());

	@Test
	void assignsLotToMachineOfFirstMatchingRule() {
		AssignmentRule bigLots = rules.save(AssignmentRule.create("Big lots", 10,
				List.of(Condition.of(LotAttribute.WAFER_COUNT, Operator.GREATER_THAN, List.of("20"))), "ETCH-02"));
		rules.save(AssignmentRule.create("Catch-all", 100, List.of(), "ETCH-01"));

		AssignmentOutcome outcome = service.autoAssign(new AutoAssignLotCommand("A100", "CMOS-7nm", 25));

		assertEquals(new AssignmentOutcome.Assigned("ETCH-02", bigLots.getId()), outcome);
		List<Lot> assigned = machines.findById("ETCH-02").orElseThrow().getAssignedLots();
		assertEquals(1, assigned.size());
		assertEquals("A100", assigned.getFirst().getChipType());
		assertTrue(machines.findById("ETCH-01").orElseThrow().getAssignedLots().isEmpty());
	}

	@Test
	void leavesLotUnassignedWhenNoRuleMatches() {
		rules.save(AssignmentRule.create("Big lots", 10,
				List.of(Condition.of(LotAttribute.WAFER_COUNT, Operator.GREATER_THAN, List.of("20"))), "ETCH-02"));

		AssignmentOutcome outcome = service.autoAssign(new AutoAssignLotCommand("A100", "CMOS-7nm", 5));

		assertInstanceOf(AssignmentOutcome.Unassigned.class, outcome);
		assertTrue(machines.findById("ETCH-02").orElseThrow().getAssignedLots().isEmpty());
	}

	@Test
	void failsWhenMatchingRuleTargetsUnknownMachine() {
		rules.save(AssignmentRule.create("Stale", 1, List.of(), "ETCH-99"));

		assertThrows(IllegalStateException.class,
				() -> service.autoAssign(new AutoAssignLotCommand("A100", "CMOS-7nm", 25)));
	}

}
