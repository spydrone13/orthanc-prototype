package com.orthanc.orthanc_prototype.assignment.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.assignment.application.port.in.ConditionSpec;
import com.orthanc.orthanc_prototype.assignment.application.port.in.SaveAssignmentRuleCommand;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleNotFoundException;
import com.orthanc.orthanc_prototype.assignment.domain.DuplicateRulePriorityException;
import com.orthanc.orthanc_prototype.assignment.domain.InvalidAssignmentRuleException;
import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.InMemoryMachineRepository;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.MachineSeedProperties;
import com.orthanc.orthanc_prototype.machine.adapter.out.memory.MachineSeedProperties.MachineSeed;

class AssignmentRuleServiceTest {

	private final InMemoryAssignmentRuleRepository rules = new InMemoryAssignmentRuleRepository();

	private final AssignmentRuleService service = new AssignmentRuleService(rules, new InMemoryMachineRepository(
			new MachineSeedProperties(List.of(new MachineSeed("ETCH-01", "Etcher 1"), new MachineSeed("ETCH-02", "Etcher 2")))));

	@Test
	void createPersistsRule() {
		AssignmentRule rule = service.create(command("X_SERIES", 10, "ETCH-01"));

		assertEquals(List.of(rule), service.listAll());
		assertEquals("X_SERIES", rule.getConditions().getFirst().operands().getFirst());
	}

	@Test
	void createRejectsUnknownMachine() {
		assertThrows(InvalidAssignmentRuleException.class, () -> service.create(command("X_SERIES", 10, "ETCH-99")));
		assertTrue(service.listAll().isEmpty());
	}

	@Test
	void createRejectsDuplicatePriority() {
		service.create(command("X_SERIES", 10, "ETCH-01"));

		assertThrows(DuplicateRulePriorityException.class, () -> service.create(command("Z_SERIES", 10, "ETCH-02")));
	}

	@Test
	void updateMayKeepItsOwnPriorityButNotTakeAnother() {
		AssignmentRule first = service.create(command("X_SERIES", 10, "ETCH-01"));
		service.create(command("Z_SERIES", 20, "ETCH-02"));

		AssignmentRule updated = service.update(first.getId(), command("Q_SERIES", 10, "ETCH-02"));
		assertEquals("ETCH-02", updated.getTargetMachineId());

		assertThrows(DuplicateRulePriorityException.class, () -> service.update(first.getId(), command("Q_SERIES", 20, "ETCH-02")));
	}

	@Test
	void setEnabledTogglesRule() {
		AssignmentRule rule = service.create(command("X_SERIES", 10, "ETCH-01"));

		assertFalse(service.setEnabled(rule.getId(), false).isEnabled());
		assertTrue(service.setEnabled(rule.getId(), true).isEnabled());
	}

	@Test
	void deleteRemovesRule() {
		AssignmentRule rule = service.create(command("X_SERIES", 10, "ETCH-01"));

		service.delete(rule.getId());

		assertThrows(AssignmentRuleNotFoundException.class, () -> service.get(rule.getId()));
	}

	@Test
	void operationsOnUnknownRuleFail() {
		AssignmentRuleId unknown = AssignmentRuleId.newId();

		assertThrows(AssignmentRuleNotFoundException.class, () -> service.get(unknown));
		assertThrows(AssignmentRuleNotFoundException.class, () -> service.update(unknown, command("X_SERIES", 1, "ETCH-01")));
		assertThrows(AssignmentRuleNotFoundException.class, () -> service.setEnabled(unknown, false));
		assertThrows(AssignmentRuleNotFoundException.class, () -> service.delete(unknown));
	}

	private static SaveAssignmentRuleCommand command(String chipType, int priority, String machineId) {
		return new SaveAssignmentRuleCommand("Rule " + chipType, priority,
				List.of(new ConditionSpec(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of(chipType))), machineId);
	}

}
