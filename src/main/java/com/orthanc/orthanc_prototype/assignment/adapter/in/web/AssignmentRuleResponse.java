package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;

record AssignmentRuleResponse(String id, String name, int priority, boolean enabled, List<ConditionDto> conditions,
		String targetMachineId) {

	static AssignmentRuleResponse from(AssignmentRule rule) {
		return new AssignmentRuleResponse(rule.getId().toString(), rule.getName(), rule.getPriority(), rule.isEnabled(),
				rule.getConditions().stream().map(ConditionDto::from).toList(), rule.getTargetMachineId());
	}

}
