package com.orthanc.orthanc_prototype.assignment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase;
import com.orthanc.orthanc_prototype.assignment.application.port.out.AssignmentRuleRepository;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleEvaluator;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.machine.application.port.out.MachineRepository;
import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

@Service
@Transactional
public class LotAutoAssignmentService implements AutoAssignLotUseCase {

	private final AssignmentRuleRepository rules;
	private final MachineRepository machines;
	private final AssignmentRuleEvaluator evaluator;

	public LotAutoAssignmentService(AssignmentRuleRepository rules, MachineRepository machines,
			AssignmentRuleEvaluator evaluator) {
		this.rules = rules;
		this.machines = machines;
		this.evaluator = evaluator;
	}

	@Override
	public AssignmentOutcome autoAssign(AutoAssignLotCommand command) {
		Lot lot = new Lot(command.chipType(), command.processType(), command.waferCount());
		return evaluator.selectRuleFor(lot, rules.findAll()).<AssignmentOutcome>map(rule -> {
			EtchingMachine machine = machines.findById(rule.getTargetMachineId())
				.orElseThrow(() -> new IllegalStateException(
						"AssignmentRule " + rule.getId() + " targets unknown machine " + rule.getTargetMachineId()));
			machine.assignLot(lot);
			machines.save(machine);
			return new AssignmentOutcome.Assigned(machine.getId(), rule.getId());
		}).orElseGet(AssignmentOutcome.Unassigned::new);
	}

}
