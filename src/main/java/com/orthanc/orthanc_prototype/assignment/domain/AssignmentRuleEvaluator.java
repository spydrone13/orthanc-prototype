package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

/**
 * Domain service that picks the rule deciding where a lot goes: the enabled rule with the lowest
 * priority whose conditions all match.
 */
public class AssignmentRuleEvaluator {

	public Optional<AssignmentRule> selectRuleFor(Lot lot, Collection<AssignmentRule> rules) {
		Objects.requireNonNull(lot, "lot must not be null");
		return rules.stream()
			.sorted(Comparator.comparingInt(AssignmentRule::getPriority))
			.filter(rule -> rule.matches(lot))
			.findFirst();
	}

}
