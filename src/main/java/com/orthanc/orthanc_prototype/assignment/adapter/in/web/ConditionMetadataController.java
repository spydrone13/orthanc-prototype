package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;

/**
 * Describes which lot attributes and operators conditions can use, so clients don't have to
 * duplicate the domain's rules.
 */
@RestController
class ConditionMetadataController {

	@GetMapping("/api/assignment-rules/condition-types")
	ConditionTypesResponse conditionTypes() {
		return new ConditionTypesResponse(
				Arrays.stream(LotAttribute.values())
					.map(attribute -> new AttributeType(attribute, attribute.kind()))
					.toList(),
				Arrays.stream(Operator.values())
					.map(operator -> new OperatorType(operator,
							operator.supportedKinds().stream().sorted().toList(),
							operator.minOperands(),
							operator.maxOperands() == Integer.MAX_VALUE ? null : operator.maxOperands()))
					.toList());
	}

	record ConditionTypesResponse(List<AttributeType> attributes, List<OperatorType> operators) {
	}

	record AttributeType(LotAttribute name, LotAttribute.Kind kind) {
	}

	record OperatorType(Operator name, List<LotAttribute.Kind> kinds, int minOperands, Integer maxOperands) {
	}

}
