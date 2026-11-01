package com.orthanc.orthanc_prototype.assignment.application.port.in;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;
import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

public interface AutoAssignLotUseCase {

	AssignmentOutcome autoAssign(AutoAssignLotCommand command);

	record AutoAssignLotCommand(ChipType chipType, ProcessType processType, int waferCount) {
	}

	sealed interface AssignmentOutcome {

		record Assigned(String machineId, AssignmentRuleId ruleId) implements AssignmentOutcome {
		}

		record Unassigned() implements AssignmentOutcome {
		}

	}

}
