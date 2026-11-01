package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.application.port.in.ConditionSpec;
import com.orthanc.orthanc_prototype.assignment.domain.Condition;
import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;

import jakarta.validation.constraints.NotNull;

record ConditionDto(@NotNull LotAttribute attribute, @NotNull Operator operator, @NotNull List<@NotNull String> values) {

	static ConditionDto from(Condition condition) {
		return new ConditionDto(condition.attribute(), condition.operator(), condition.operands());
	}

	ConditionSpec toSpec() {
		return new ConditionSpec(attribute, operator, values);
	}

}
