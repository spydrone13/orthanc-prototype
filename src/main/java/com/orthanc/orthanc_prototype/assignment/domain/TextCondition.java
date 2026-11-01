package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

public final class TextCondition implements Condition {

	private final LotAttribute attribute;
	private final Operator operator;
	private final List<String> operands;
	private final Pattern pattern;

	TextCondition(LotAttribute attribute, Operator operator, List<String> operands) {
		this.attribute = attribute;
		this.operator = operator;
		this.operands = List.copyOf(operands);
		if (this.operands.stream().anyMatch(String::isBlank)) {
			throw new InvalidAssignmentRuleException("Operands for " + attribute + " must not be blank");
		}
		this.pattern = operator == Operator.MATCHES ? compile(this.operands.getFirst()) : null;
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
		String value = attribute.textValueOf(lot);
		return switch (operator) {
			case EQUALS -> value.equals(operands.getFirst());
			case IN -> operands.contains(value);
			case STARTS_WITH -> value.startsWith(operands.getFirst());
			case MATCHES -> pattern.matcher(value).matches();
			default -> throw new IllegalStateException("Unsupported text operator " + operator);
		};
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof TextCondition that && attribute == that.attribute && operator == that.operator
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

	private static Pattern compile(String regex) {
		try {
			return Pattern.compile(regex);
		}
		catch (PatternSyntaxException ex) {
			throw new InvalidAssignmentRuleException("Invalid regular expression '" + regex + "': " + ex.getDescription());
		}
	}

}
