package com.orthanc.orthanc_prototype.assignment.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRule;
import com.orthanc.orthanc_prototype.assignment.domain.AssignmentRuleId;
import com.orthanc.orthanc_prototype.assignment.domain.Condition;
import com.orthanc.orthanc_prototype.assignment.domain.LotAttribute;
import com.orthanc.orthanc_prototype.assignment.domain.Operator;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(AssignmentRulePersistenceAdapter.class)
class AssignmentRulePersistenceAdapterTest {

	@Autowired
	private AssignmentRulePersistenceAdapter adapter;

	@Autowired
	private EntityManager entityManager;

	@Test
	void roundTripsRuleWithOrderedConditionsAndOperands() {
		List<Condition> conditions = List.of(
				Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("C300", "A100", "B200")),
				Condition.of(LotAttribute.PROCESS_TYPE, Operator.MATCHES, List.of("CMOS-\\d+nm")),
				Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of("10", "25")));
		AssignmentRule rule = AssignmentRule.create("Mixed", 7, conditions, "ETCH-01");
		rule.disable();

		adapter.save(rule);
		entityManager.clear();
		AssignmentRule loaded = adapter.findById(rule.getId()).orElseThrow();

		assertEquals(rule.getId(), loaded.getId());
		assertEquals("Mixed", loaded.getName());
		assertEquals(7, loaded.getPriority());
		assertFalse(loaded.isEnabled());
		assertEquals("ETCH-01", loaded.getTargetMachineId());
		assertEquals(conditions, loaded.getConditions());
	}

	@Test
	void saveOfExistingRuleReplacesConditions() {
		AssignmentRule rule = AssignmentRule.create("R", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("A100")),
						Condition.of(LotAttribute.WAFER_COUNT, Operator.LESS_THAN, List.of("5"))),
				"ETCH-01");
		adapter.save(rule);

		Condition replacement = Condition.of(LotAttribute.WAFER_COUNT, Operator.GREATER_THAN_OR_EQUAL, List.of("20"));
		rule.update("R2", 2, List.of(replacement), "ETCH-02");
		adapter.save(rule);
		entityManager.clear();

		AssignmentRule loaded = adapter.findById(rule.getId()).orElseThrow();
		assertEquals("R2", loaded.getName());
		assertEquals(List.of(replacement), loaded.getConditions());
		assertEquals(1, adapter.findAll().size());
	}

	@Test
	void findAllReturnsRulesOrderedByPriority() {
		AssignmentRule low = adapter.save(AssignmentRule.create("Low", 50, List.of(), "ETCH-01"));
		AssignmentRule high = adapter.save(AssignmentRule.create("High", 5, List.of(), "ETCH-01"));
		AssignmentRule mid = adapter.save(AssignmentRule.create("Mid", 20, List.of(), "ETCH-01"));
		entityManager.clear();

		assertEquals(List.of(high, mid, low), adapter.findAll());
	}

	@Test
	void existsOtherWithPriorityIgnoresExcludedRule() {
		AssignmentRule rule = adapter.save(AssignmentRule.create("R", 3, List.of(), "ETCH-01"));

		assertFalse(adapter.existsOtherWithPriority(3, rule.getId()));
		assertTrue(adapter.existsOtherWithPriority(3, AssignmentRuleId.newId()));
		assertFalse(adapter.existsOtherWithPriority(4, AssignmentRuleId.newId()));
	}

	@Test
	void deleteRemovesRuleAndConditions() {
		AssignmentRule rule = adapter.save(AssignmentRule.create("R", 1,
				List.of(Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("A100"))), "ETCH-01"));

		adapter.deleteById(rule.getId());
		entityManager.flush();

		assertTrue(adapter.findById(rule.getId()).isEmpty());
		assertEquals(0L, entityManager.createQuery("select count(c) from ConditionJpaEntity c", Long.class)
			.getSingleResult());
	}

}
