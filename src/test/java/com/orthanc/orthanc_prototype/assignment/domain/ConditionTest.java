package com.orthanc.orthanc_prototype.assignment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

class ConditionTest {

	private static final Lot LOT = new Lot("A100", "CMOS-7nm", 25);

	@ParameterizedTest
	@CsvSource(delimiter = '|', textBlock = """
			CHIP_TYPE    | EQUALS                | A100          | true
			CHIP_TYPE    | EQUALS                | B200          | false
			CHIP_TYPE    | IN                    | B200,A100     | true
			CHIP_TYPE    | IN                    | B200,C300     | false
			PROCESS_TYPE | STARTS_WITH           | CMOS          | true
			PROCESS_TYPE | STARTS_WITH           | BiCMOS        | false
			PROCESS_TYPE | MATCHES               | CMOS-\\d+nm   | true
			PROCESS_TYPE | MATCHES               | CMOS          | false
			WAFER_COUNT  | EQUALS                | 25            | true
			WAFER_COUNT  | IN                    | 20,25         | true
			WAFER_COUNT  | IN                    | 20,24         | false
			WAFER_COUNT  | LESS_THAN             | 25            | false
			WAFER_COUNT  | LESS_THAN_OR_EQUAL    | 25            | true
			WAFER_COUNT  | GREATER_THAN          | 24            | true
			WAFER_COUNT  | GREATER_THAN          | 25            | false
			WAFER_COUNT  | GREATER_THAN_OR_EQUAL | 25            | true
			WAFER_COUNT  | BETWEEN               | 10,25         | true
			WAFER_COUNT  | BETWEEN               | 25,30         | true
			WAFER_COUNT  | BETWEEN               | 26,30         | false
			""")
	void matchesLotAttribute(LotAttribute attribute, Operator operator, String operands, boolean expected) {
		Condition condition = Condition.of(attribute, operator, Arrays.asList(operands.split(",")));

		assertEquals(expected, condition.matches(LOT));
	}

	@Test
	void createsTypedConditionPerAttributeKind() {
		assertInstanceOf(TextCondition.class, Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("A100")));
		assertInstanceOf(WaferCountCondition.class,
				Condition.of(LotAttribute.WAFER_COUNT, Operator.EQUALS, List.of("25")));
	}

	@Test
	void exposesOperandsInStringForm() {
		Condition condition = Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of(" 10", "25"));

		assertEquals(List.of("10", "25"), condition.operands());
	}

	@Test
	void rejectsOperatorNotSupportedForAttribute() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.GREATER_THAN, List.of("A")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.STARTS_WITH, List.of("2")));
	}

	@Test
	void rejectsWrongOperandCount() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of()));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("A", "B")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of()));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of("1")));
	}

	@Test
	void rejectsInvalidOperands() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.EQUALS, List.of("many")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of(" ")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.PROCESS_TYPE, Operator.MATCHES, List.of("CMOS-[")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of("30", "10")));
	}

	@Test
	void rejectsNullArguments() {
		assertThrows(InvalidAssignmentRuleException.class, () -> Condition.of(null, Operator.EQUALS, List.of("A")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, null, List.of("A")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, null));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, Arrays.asList((String) null)));
	}

	@Test
	void conditionsWithSameDefinitionAreEqual() {
		Condition first = Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("A", "B"));
		Condition second = Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("A", "B"));

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertTrue(first.matches(new Lot("B", "X", 1)));
		assertFalse(first.matches(new Lot("C", "X", 1)));
	}

}
