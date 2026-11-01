package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.application.port.in.SaveAssignmentRuleCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

record AssignmentRuleRequest(
		@NotBlank String name,
		@NotNull @PositiveOrZero Integer priority,
		@NotNull List<@NotNull @Valid ConditionDto> conditions,
		@NotBlank String targetMachineId) {

	SaveAssignmentRuleCommand toCommand() {
		return new SaveAssignmentRuleCommand(name, priority, conditions.stream().map(ConditionDto::toSpec).toList(),
				targetMachineId);
	}

}
