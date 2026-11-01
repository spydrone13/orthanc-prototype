package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.List;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

/**
 * A single test on a {@link Lot} attribute. Operands are kept in their string form so a condition
 * can be round-tripped through any adapter via {@link #of}.
 */
public sealed interface Condition permits EnumCondition, WaferCountCondition {

	LotAttribute attribute();

	Operator operator();

	List<String> operands();

	boolean matches(Lot lot);

	static Condition of(LotAttribute attribute, Operator operator, List<String> operands) {
		if (attribute == null) {
			throw new InvalidAssignmentRuleException("Condition attribute must not be null");
		}
		if (operator == null) {
			throw new InvalidAssignmentRuleException("Condition operator must not be null");
		}
		if (operands == null || operands.stream().anyMatch(operand -> operand == null)) {
			throw new InvalidAssignmentRuleException("Condition operands must not be null");
		}
		if (!operator.supports(attribute.kind())) {
			throw new InvalidAssignmentRuleException("Operator " + operator + " is not supported for " + attribute);
		}
		operator.validateOperandCount(operands.size());
		return switch (attribute.kind()) {
			case ENUMERATED -> new EnumCondition(attribute, operator, operands);
			case NUMERIC -> new WaferCountCondition(operator, operands);
		};
	}

}
