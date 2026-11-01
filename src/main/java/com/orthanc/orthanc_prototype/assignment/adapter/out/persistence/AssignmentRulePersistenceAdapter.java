package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.orthanc.orthanc_prototype.assignment.application.port.out.AssignmentRuleRepository;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;

@Repository
class AssignmentRulePersistenceAdapter implements AssignmentRuleRepository {

	private final SpringDataAssignmentRuleRepository jpaRepository;

	AssignmentRulePersistenceAdapter(SpringDataAssignmentRuleRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public AssignmentRule save(AssignmentRule rule) {
		AssignmentRuleJpaEntity entity = jpaRepository.findById(rule.getId().value())
			.orElseGet(() -> new AssignmentRuleJpaEntity(rule.getId().value()));
		AssignmentRuleMapper.copyToEntity(rule, entity);
		return AssignmentRuleMapper.toDomain(jpaRepository.saveAndFlush(entity));
	}

	@Override
	public Optional<AssignmentRule> findById(AssignmentRuleId id) {
		return jpaRepository.findById(id.value()).map(AssignmentRuleMapper::toDomain);
	}

	@Override
	public List<AssignmentRule> findAll() {
		return jpaRepository.findAllByOrderByPriorityAsc().stream().map(AssignmentRuleMapper::toDomain).toList();
	}

	@Override
	public void deleteById(AssignmentRuleId id) {
		jpaRepository.deleteById(id.value());
	}

	@Override
	public boolean existsOtherWithPriority(int priority, AssignmentRuleId excludedId) {
		return jpaRepository.existsByPriorityAndIdNot(priority, excludedId.value());
	}

}
