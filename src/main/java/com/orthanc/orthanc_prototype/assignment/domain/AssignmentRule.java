package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.List;
import java.util.Objects;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

/**
 * Aggregate root: routes lots whose properties satisfy all {@link #getConditions() conditions} to
 * the target machine. Rules with a lower priority value are evaluated first. A rule without
 * conditions matches every lot.
 */
public class AssignmentRule {

	private final AssignmentRuleId id;
	private String name;
	private int priority;
	private boolean enabled;
	private List<Condition> conditions;
	private String targetMachineId;

	private AssignmentRule(AssignmentRuleId id, String name, int priority, boolean enabled,
			List<Condition> conditions, String targetMachineId) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.enabled = enabled;
		update(name, priority, conditions, targetMachineId);
	}

	public static AssignmentRule create(String name, int priority, List<Condition> conditions, String targetMachineId) {
		return new AssignmentRule(AssignmentRuleId.newId(), name, priority, true, conditions, targetMachineId);
	}

	public static AssignmentRule rehydrate(AssignmentRuleId id, String name, int priority, boolean enabled,
			List<Condition> conditions, String targetMachineId) {
		return new AssignmentRule(id, name, priority, enabled, conditions, targetMachineId);
	}

	public void update(String name, int priority, List<Condition> conditions, String targetMachineId) {
		if (priority < 0) {
			throw new InvalidAssignmentRuleException("priority must be >= 0, was " + priority);
		}
		if (conditions == null || conditions.stream().anyMatch(Objects::isNull)) {
			throw new InvalidAssignmentRuleException("conditions must not be null");
		}
		this.name = requireNonBlank(name, "name");
		this.priority = priority;
		this.conditions = List.copyOf(conditions);
		this.targetMachineId = requireNonBlank(targetMachineId, "targetMachineId");
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	public boolean matches(Lot lot) {
		Objects.requireNonNull(lot, "lot must not be null");
		return enabled && conditions.stream().allMatch(condition -> condition.matches(lot));
	}

	public AssignmentRuleId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public String getTargetMachineId() {
		return targetMachineId;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof AssignmentRule that && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "AssignmentRule{id=" + id + ", name='" + name + "', priority=" + priority + ", enabled=" + enabled
				+ ", conditions=" + conditions + ", targetMachineId='" + targetMachineId + "'}";
	}

	private static String requireNonBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new InvalidAssignmentRuleException(name + " must not be blank");
		}
		return value;
	}

}
