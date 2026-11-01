package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute.Kind;

/**
 * Comparison applied by a {@link Condition}. Each operator declares which attribute kinds it
 * supports and how many operands it takes.
 */
public enum Operator {

	EQUALS(1, 1, Kind.TEXT, Kind.NUMERIC),
	IN(1, Integer.MAX_VALUE, Kind.TEXT, Kind.NUMERIC),
	STARTS_WITH(1, 1, Kind.TEXT),
	MATCHES(1, 1, Kind.TEXT),
	LESS_THAN(1, 1, Kind.NUMERIC),
	LESS_THAN_OR_EQUAL(1, 1, Kind.NUMERIC),
	GREATER_THAN(1, 1, Kind.NUMERIC),
	GREATER_THAN_OR_EQUAL(1, 1, Kind.NUMERIC),
	/** Inclusive range: {@code [min, max]}. */
	BETWEEN(2, 2, Kind.NUMERIC);

	private final int minOperands;
	private final int maxOperands;
	private final Set<Kind> supportedKinds;

	Operator(int minOperands, int maxOperands, Kind first, Kind... rest) {
		this.minOperands = minOperands;
		this.maxOperands = maxOperands;
		this.supportedKinds = EnumSet.of(first, rest);
	}

	public boolean supports(Kind kind) {
		return supportedKinds.contains(kind);
	}

	public Set<Kind> supportedKinds() {
		return Collections.unmodifiableSet(supportedKinds);
	}

	public int minOperands() {
		return minOperands;
	}

	/** Maximum number of operands, or {@link Integer#MAX_VALUE} when unbounded. */
	public int maxOperands() {
		return maxOperands;
	}

	void validateOperandCount(int count) {
		if (count < minOperands || count > maxOperands) {
			String expected = minOperands == maxOperands ? String.valueOf(minOperands)
					: maxOperands == Integer.MAX_VALUE ? "at least " + minOperands
					: minOperands + ".." + maxOperands;
			throw new InvalidAssignmentRuleException(
					"Operator " + this + " expects " + expected + " operand(s), got " + count);
		}
	}

}
