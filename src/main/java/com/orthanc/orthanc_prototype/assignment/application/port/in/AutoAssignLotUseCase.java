package com.orthanc.orthanc_prototype.assignment.application.port.in;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

public interface AutoAssignLotUseCase {

	AssignmentOutcome autoAssign(AutoAssignLotCommand command);

	record AutoAssignLotCommand(String chipType, String processType, int waferCount) {
	}

	sealed interface AssignmentOutcome {

		record Assigned(String machineId, AssignmentRuleId ruleId) implements AssignmentOutcome {
		}

		record Unassigned() implements AssignmentOutcome {
		}

	}

}
