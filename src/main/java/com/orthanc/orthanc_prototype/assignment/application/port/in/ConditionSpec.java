package com.orthanc.orthanc_prototype.assignment.application.port.in;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.domain.Condition;
import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;

public record ConditionSpec(LotAttribute attribute, Operator operator, List<String> operands) {

	public Condition toCondition() {
		return Condition.of(attribute, operator, operands);
	}

}
