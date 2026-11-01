package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase;
import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase.AssignmentOutcome;
import com.orthanc.orthanc_prototype.assignment.application.port.in.AutoAssignLotUseCase.AutoAssignLotCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
class LotAssignmentController {

	private final AutoAssignLotUseCase autoAssignLot;

	LotAssignmentController(AutoAssignLotUseCase autoAssignLot) {
		this.autoAssignLot = autoAssignLot;
	}

	@PostMapping("/api/lots/auto-assignments")
	AutoAssignLotResponse autoAssign(@Valid @RequestBody AutoAssignLotRequest request) {
		AssignmentOutcome outcome = autoAssignLot
			.autoAssign(new AutoAssignLotCommand(request.chipType(), request.processType(), request.waferCount()));
		return switch (outcome) {
			case AssignmentOutcome.Assigned assigned ->
				new AutoAssignLotResponse("ASSIGNED", assigned.machineId(), assigned.ruleId().toString());
			case AssignmentOutcome.Unassigned unassigned -> new AutoAssignLotResponse("UNASSIGNED", null, null);
		};
	}

	record AutoAssignLotRequest(@NotBlank String chipType, @NotBlank String processType,
			@NotNull @PositiveOrZero Integer waferCount) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record AutoAssignLotResponse(String status, String machineId, String ruleId) {
	}

}
