package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAssignmentRuleRepository extends JpaRepository<AssignmentRuleJpaEntity, UUID> {

	@EntityGraph(attributePaths = { "conditions", "conditions.operands" })
	List<AssignmentRuleJpaEntity> findAllByOrderByPriorityAsc();

	boolean existsByPriorityAndIdNot(int priority, UUID id);

}
