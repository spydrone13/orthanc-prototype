package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.List;
import java.util.Objects;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

/**
 * Condition on an {@link LotAttribute.Kind#ENUMERATED} attribute; operands must be names of the
 * attribute's {@link LotAttribute#allowedValues() allowed values}.
 */
public final class EnumCondition implements Condition {

	private final LotAttribute attribute;
	private final Operator operator;
	private final List<String> operands;

	EnumCondition(LotAttribute attribute, Operator operator, List<String> operands) {
		this.attribute = attribute;
		this.operator = operator;
		this.operands = operands.stream().map(String::strip).toList();
		for (String operand : this.operands) {
			if (!attribute.allowedValues().contains(operand)) {
				throw new InvalidAssignmentRuleException("Unknown " + attribute + " value '" + operand + "'; allowed: "
						+ String.join(", ", attribute.allowedValues()));
			}
		}
	}

	@Override
	public LotAttribute attribute() {
		return attribute;
	}

	@Override
	public Operator operator() {
		return operator;
	}

	@Override
	public List<String> operands() {
		return operands;
	}

	@Override
	public boolean matches(Lot lot) {
		String value = attribute.enumValueOf(lot);
		return switch (operator) {
			case EQUALS -> value.equals(operands.getFirst());
			case IN -> operands.contains(value);
			case NOT_IN -> !operands.contains(value);
			default -> throw new IllegalStateException("Unsupported enumerated operator " + operator);
		};
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof EnumCondition that && attribute == that.attribute && operator == that.operator
				&& operands.equals(that.operands);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attribute, operator, operands);
	}

	@Override
	public String toString() {
		return attribute + " " + operator + " " + operands;
	}

}
