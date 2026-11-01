package com.orthanc.orthanc_prototype.assignment.domain;

public class DuplicateRulePriorityException extends IllegalStateException {

	public DuplicateRulePriorityException(int priority) {
		super("Another AssignmentRule already has priority " + priority);
	}

}
