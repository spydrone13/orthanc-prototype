package com.orthanc.orthanc_prototype.assignment.application.port.out;

import java.util.List;
import java.util.Optional;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

public interface AssignmentRuleRepository {

	AssignmentRule save(AssignmentRule rule);

	Optional<AssignmentRule> findById(AssignmentRuleId id);

	/** All rules ordered by ascending priority. */
	List<AssignmentRule> findAll();

	void deleteById(AssignmentRuleId id);

	boolean existsOtherWithPriority(int priority, AssignmentRuleId excludedId);

}
