package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.Objects;
import java.util.UUID;

public record AssignmentRuleId(UUID value) {

	public AssignmentRuleId {
		Objects.requireNonNull(value, "value must not be null");
	}

	public static AssignmentRuleId newId() {
		return new AssignmentRuleId(UUID.randomUUID());
	}

	public static AssignmentRuleId of(String value) {
		try {
			return new AssignmentRuleId(UUID.fromString(value));
		}
		catch (IllegalArgumentException | NullPointerException ex) {
			throw new InvalidAssignmentRuleException("Invalid AssignmentRule id: " + value);
		}
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
