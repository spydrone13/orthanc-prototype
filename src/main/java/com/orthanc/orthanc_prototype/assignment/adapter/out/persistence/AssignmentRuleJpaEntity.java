package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "assignment_rule")
class AssignmentRuleJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private int priority;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "target_machine_id", nullable = false)
	private String targetMachineId;

	@Version
	private Long version;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "rule_id", nullable = false)
	@OrderColumn(name = "condition_index")
	private List<ConditionJpaEntity> conditions = new ArrayList<>();

	protected AssignmentRuleJpaEntity() {
	}

	AssignmentRuleJpaEntity(UUID id) {
		this.id = id;
	}

	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	int getPriority() {
		return priority;
	}

	void setPriority(int priority) {
		this.priority = priority;
	}

	boolean isEnabled() {
		return enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	String getTargetMachineId() {
		return targetMachineId;
	}

	void setTargetMachineId(String targetMachineId) {
		this.targetMachineId = targetMachineId;
	}

	List<ConditionJpaEntity> getConditions() {
		return conditions;
	}

	void replaceConditions(List<ConditionJpaEntity> newConditions) {
		conditions.clear();
		conditions.addAll(newConditions);
	}

}
