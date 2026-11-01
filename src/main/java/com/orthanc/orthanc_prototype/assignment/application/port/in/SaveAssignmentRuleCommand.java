package com.orthanc.orthanc_prototype.assignment.application.port.in;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.domain.Condition;
import com.orthanc.orthanc_prototype.assignment.domain.InvalidAssignmentRuleException;

public record SaveAssignmentRuleCommand(String name, int priority, List<ConditionSpec> conditions,
		String targetMachineId) {

	public List<Condition> toConditions() {
		if (conditions == null) {
			throw new InvalidAssignmentRuleException("conditions must not be null");
		}
		return conditions.stream().map(ConditionSpec::toCondition).toList();
	}

}
