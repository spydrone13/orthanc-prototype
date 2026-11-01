package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignment_rule_condition")
class ConditionJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 32)
	private LotAttribute attribute;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 32)
	private Operator operator;

	@ElementCollection
	@CollectionTable(name = "assignment_rule_condition_operand", joinColumns = @JoinColumn(name = "condition_id"))
	@OrderColumn(name = "operand_index")
	@Column(name = "operand_value", nullable = false, length = 1024)
	private List<String> operands = new ArrayList<>();

	protected ConditionJpaEntity() {
	}

	ConditionJpaEntity(LotAttribute attribute, Operator operator, List<String> operands) {
		this.attribute = attribute;
		this.operator = operator;
		this.operands = new ArrayList<>(operands);
	}

	LotAttribute getAttribute() {
		return attribute;
	}

	Operator getOperator() {
		return operator;
	}

	List<String> getOperands() {
		return operands;
	}

}
