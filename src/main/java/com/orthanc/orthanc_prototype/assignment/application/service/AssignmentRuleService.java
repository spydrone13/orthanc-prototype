package com.orthanc.orthanc_prototype.assignment.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orthanc.orthanc_prototype.assignment.application.port.in.ManageAssignmentRulesUseCase;
import com.orthanc.orthanc_prototype.assignment.application.port.in.SaveAssignmentRuleCommand;
import com.orthanc.orthanc_prototype.assignment.application.port.out.AssignmentRuleRepository;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleNotFoundException;
import com.orthanc.orthanc_prototype.assignment.domain.DuplicateRulePriorityException;
import com.orthanc.orthanc_prototype.assignment.domain.InvalidAssignmentRuleException;
import com.orthanc.orthanc_prototype.machine.application.port.out.MachineRepository;

@Service
@Transactional
public class AssignmentRuleService implements ManageAssignmentRulesUseCase {

	private final AssignmentRuleRepository rules;
	private final MachineRepository machines;

	public AssignmentRuleService(AssignmentRuleRepository rules, MachineRepository machines) {
		this.rules = rules;
		this.machines = machines;
	}

	@Override
	public AssignmentRule create(SaveAssignmentRuleCommand command) {
		AssignmentRule rule = AssignmentRule.create(command.name(), command.priority(), command.toConditions(),
				command.targetMachineId());
		return validateAndSave(rule);
	}

	@Override
	public AssignmentRule update(AssignmentRuleId id, SaveAssignmentRuleCommand command) {
		AssignmentRule rule = load(id);
		rule.update(command.name(), command.priority(), command.toConditions(), command.targetMachineId());
		return validateAndSave(rule);
	}

	@Override
	public AssignmentRule setEnabled(AssignmentRuleId id, boolean enabled) {
		AssignmentRule rule = load(id);
		if (enabled) {
			rule.enable();
		}
		else {
			rule.disable();
		}
		return rules.save(rule);
	}

	@Override
	public void delete(AssignmentRuleId id) {
		load(id);
		rules.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public AssignmentRule get(AssignmentRuleId id) {
		return load(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AssignmentRule> listAll() {
		return rules.findAll();
	}

	private AssignmentRule load(AssignmentRuleId id) {
		return rules.findById(id).orElseThrow(() -> new AssignmentRuleNotFoundException(id));
	}

	private AssignmentRule validateAndSave(AssignmentRule rule) {
		if (!machines.existsById(rule.getTargetMachineId())) {
			throw new InvalidAssignmentRuleException("Unknown target machine: " + rule.getTargetMachineId());
		}
		if (rules.existsOtherWithPriority(rule.getPriority(), rule.getId())) {
			throw new DuplicateRulePriorityException(rule.getPriority());
		}
		return rules.save(rule);
	}

}
