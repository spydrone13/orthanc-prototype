package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.List;
import java.util.Objects;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

public final class WaferCountCondition implements Condition {

	private final Operator operator;
	private final List<Integer> values;

	WaferCountCondition(Operator operator, List<String> operands) {
		this.operator = operator;
		this.values = operands.stream().map(WaferCountCondition::parse).toList();
		if (operator == Operator.BETWEEN && values.get(0) > values.get(1)) {
			throw new InvalidAssignmentRuleException(
					"BETWEEN lower bound " + values.get(0) + " must be <= upper bound " + values.get(1));
		}
	}

	@Override
	public LotAttribute attribute() {
		return LotAttribute.WAFER_COUNT;
	}

	@Override
	public Operator operator() {
		return operator;
	}

	@Override
	public List<String> operands() {
		return values.stream().map(String::valueOf).toList();
	}

	@Override
	public boolean matches(Lot lot) {
		int value = LotAttribute.WAFER_COUNT.numericValueOf(lot);
		return switch (operator) {
			case EQUALS -> value == values.getFirst();
			case IN -> values.contains(value);
			case NOT_IN -> !values.contains(value);
			case LESS_THAN -> value < values.getFirst();
			case LESS_THAN_OR_EQUAL -> value <= values.getFirst();
			case GREATER_THAN -> value > values.getFirst();
			case GREATER_THAN_OR_EQUAL -> value >= values.getFirst();
			case BETWEEN -> value >= values.get(0) && value <= values.get(1);
			default -> throw new IllegalStateException("Unsupported numeric operator " + operator);
		};
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof WaferCountCondition that && operator == that.operator && values.equals(that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(operator, values);
	}

	@Override
	public String toString() {
		return LotAttribute.WAFER_COUNT + " " + operator + " " + values;
	}

	private static int parse(String operand) {
		try {
			return Integer.parseInt(operand.strip());
		}
		catch (NumberFormatException ex) {
			throw new InvalidAssignmentRuleException("Operand for WAFER_COUNT must be an integer, was '" + operand + "'");
		}
	}

}
