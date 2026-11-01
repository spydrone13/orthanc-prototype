package com.orthanc.orthanc_prototype.assignment.application.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orthanc.orthanc_prototype.assignment.application.port.out.AssignmentRuleRepository;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

class InMemoryAssignmentRuleRepository implements AssignmentRuleRepository {

	private final Map<AssignmentRuleId, AssignmentRule> rules = new LinkedHashMap<>();

	@Override
	public AssignmentRule save(AssignmentRule rule) {
		rules.put(rule.getId(), rule);
		return rule;
	}

	@Override
	public Optional<AssignmentRule> findById(AssignmentRuleId id) {
		return Optional.ofNullable(rules.get(id));
	}

	@Override
	public List<AssignmentRule> findAll() {
		return rules.values().stream().sorted(Comparator.comparingInt(AssignmentRule::getPriority)).toList();
	}

	@Override
	public void deleteById(AssignmentRuleId id) {
		rules.remove(id);
	}

	@Override
	public boolean existsOtherWithPriority(int priority, AssignmentRuleId excludedId) {
		return rules.values().stream()
			.anyMatch(rule -> rule.getPriority() == priority && !rule.getId().equals(excludedId));
	}

}
