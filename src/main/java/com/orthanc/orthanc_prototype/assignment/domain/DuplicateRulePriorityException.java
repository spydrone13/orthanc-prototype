package com.orthanc.orthanc_prototype.assignment.domain;

public class DuplicateRulePriorityException extends IllegalStateException {

	public DuplicateRulePriorityException(int priority) {
		super("Another rule already has priority " + priority);
	}

}
