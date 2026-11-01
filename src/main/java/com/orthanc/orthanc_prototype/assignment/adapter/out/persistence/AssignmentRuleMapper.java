package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;
import com.orthanc.orthanc_prototype.assignment.domain.Condition;

final class AssignmentRuleMapper {

	private AssignmentRuleMapper() {
	}

	static AssignmentRule toDomain(AssignmentRuleJpaEntity entity) {
		return AssignmentRule.rehydrate(
				new AssignmentRuleId(entity.getId()),
				entity.getName(),
				entity.getPriority(),
				entity.isEnabled(),
				entity.getConditions().stream()
					.map(condition -> Condition.of(condition.getAttribute(), condition.getOperator(),
							condition.getOperands()))
					.toList(),
				entity.getTargetMachineId());
	}

	static void copyToEntity(AssignmentRule rule, AssignmentRuleJpaEntity entity) {
		entity.setName(rule.getName());
		entity.setPriority(rule.getPriority());
		entity.setEnabled(rule.isEnabled());
		entity.setTargetMachineId(rule.getTargetMachineId());
		entity.replaceConditions(rule.getConditions().stream()
			.map(condition -> new ConditionJpaEntity(condition.attribute(), condition.operator(), condition.operands()))
			.toList());
	}

}
