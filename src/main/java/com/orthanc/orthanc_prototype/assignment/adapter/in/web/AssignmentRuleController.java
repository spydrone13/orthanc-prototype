package com.orthanc.orthanc_prototype.assignment.adapter.in.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orthanc.orthanc_prototype.assignment.application.port.in.ManageAssignmentRulesUseCase;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/assignment-rules")
class AssignmentRuleController {

	private final ManageAssignmentRulesUseCase manageRules;

	AssignmentRuleController(ManageAssignmentRulesUseCase manageRules) {
		this.manageRules = manageRules;
	}

	@PostMapping
	ResponseEntity<AssignmentRuleResponse> create(@Valid @RequestBody AssignmentRuleRequest request) {
		AssignmentRule rule = manageRules.create(request.toCommand());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(rule.getId())
			.toUri();
		return ResponseEntity.created(location).body(AssignmentRuleResponse.from(rule));
	}

	@GetMapping
	List<AssignmentRuleResponse> list() {
		return manageRules.listAll().stream().map(AssignmentRuleResponse::from).toList();
	}

	@GetMapping("/{id}")
	AssignmentRuleResponse get(@PathVariable String id) {
		return AssignmentRuleResponse.from(manageRules.get(AssignmentRuleId.of(id)));
	}

	@PutMapping("/{id}")
	AssignmentRuleResponse update(@PathVariable String id, @Valid @RequestBody AssignmentRuleRequest request) {
		return AssignmentRuleResponse.from(manageRules.update(AssignmentRuleId.of(id), request.toCommand()));
	}

	@PatchMapping("/{id}/enabled")
	AssignmentRuleResponse setEnabled(@PathVariable String id, @Valid @RequestBody EnabledRequest request) {
		return AssignmentRuleResponse.from(manageRules.setEnabled(AssignmentRuleId.of(id), request.enabled()));
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> delete(@PathVariable String id) {
		manageRules.delete(AssignmentRuleId.of(id));
		return ResponseEntity.noContent().build();
	}

	record EnabledRequest(@NotNull Boolean enabled) {
	}

}
