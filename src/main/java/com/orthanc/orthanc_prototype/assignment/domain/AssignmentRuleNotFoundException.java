package com.orthanc.orthanc_prototype.assignment.domain;

public class AssignmentRuleNotFoundException extends RuntimeException {

	public AssignmentRuleNotFoundException(AssignmentRuleId id) {
		super("AssignmentRule not found: " + id);
	}

}
