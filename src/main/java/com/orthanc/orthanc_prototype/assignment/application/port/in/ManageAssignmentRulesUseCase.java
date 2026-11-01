package com.orthanc.orthanc_prototype.assignment.application.port.in;

import java.util.List;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

public interface ManageAssignmentRulesUseCase {

	AssignmentRule create(SaveAssignmentRuleCommand command);

	AssignmentRule update(AssignmentRuleId id, SaveAssignmentRuleCommand command);

	AssignmentRule setEnabled(AssignmentRuleId id, boolean enabled);

	void delete(AssignmentRuleId id);

	AssignmentRule get(AssignmentRuleId id);

	/** All rules in evaluation order (ascending priority). */
	List<AssignmentRule> listAll();

}
