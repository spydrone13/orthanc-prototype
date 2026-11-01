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

import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

class ConditionTest {

	private static final Lot LOT = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

	@ParameterizedTest
	@CsvSource(delimiter = '|', textBlock = """
			CHIP_TYPE    | EQUALS                | X_SERIES          | true
			CHIP_TYPE    | EQUALS                | Z_SERIES          | false
			CHIP_TYPE    | IN                    | Z_SERIES,X_SERIES | true
			CHIP_TYPE    | IN                    | Z_SERIES,Q_SERIES | false
			CHIP_TYPE    | NOT_IN                | Z_SERIES,Q_SERIES | true
			CHIP_TYPE    | NOT_IN                | X_SERIES          | false
			PROCESS_TYPE | EQUALS                | STANDARD          | true
			PROCESS_TYPE | IN                    | LEGACY,NEXTGEN    | false
			PROCESS_TYPE | NOT_IN                | LEGACY            | true
			WAFER_COUNT  | EQUALS                | 25                | true
			WAFER_COUNT  | IN                    | 20,25             | true
			WAFER_COUNT  | IN                    | 20,24             | false
			WAFER_COUNT  | NOT_IN                | 20,24             | true
			WAFER_COUNT  | NOT_IN                | 25                | false
			WAFER_COUNT  | LESS_THAN             | 25                | false
			WAFER_COUNT  | LESS_THAN_OR_EQUAL    | 25                | true
			WAFER_COUNT  | GREATER_THAN          | 24                | true
			WAFER_COUNT  | GREATER_THAN          | 25                | false
			WAFER_COUNT  | GREATER_THAN_OR_EQUAL | 25                | true
			WAFER_COUNT  | BETWEEN               | 10,25             | true
			WAFER_COUNT  | BETWEEN               | 25,30             | true
			WAFER_COUNT  | BETWEEN               | 26,30             | false
			""")
	void matchesLotAttribute(LotAttribute attribute, Operator operator, String operands, boolean expected) {
		Condition condition = Condition.of(attribute, operator, Arrays.asList(operands.split(",")));

		assertEquals(expected, condition.matches(LOT));
	}

	@Test
	void createsTypedConditionPerAttributeKind() {
		assertInstanceOf(EnumCondition.class, Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("X_SERIES")));
		assertInstanceOf(WaferCountCondition.class,
				Condition.of(LotAttribute.WAFER_COUNT, Operator.EQUALS, List.of("25")));
	}

	@Test
	void exposesOperandsInNormalisedStringForm() {
		assertEquals(List.of("10", "25"),
				Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of(" 10", "25")).operands());
		assertEquals(List.of("X_SERIES"),
				Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of(" X_SERIES ")).operands());
	}

	@Test
	void enumeratedAttributesExposeTheirAllowedValues() {
		assertEquals(List.of("X_SERIES", "Z_SERIES", "Q_SERIES"), LotAttribute.CHIP_TYPE.allowedValues());
		assertEquals(List.of("LEGACY", "STANDARD", "NEXTGEN"), LotAttribute.PROCESS_TYPE.allowedValues());
		assertTrue(LotAttribute.WAFER_COUNT.allowedValues().isEmpty());
	}

	@Test
	void rejectsUnknownEnumValues() {
		InvalidAssignmentRuleException ex = assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("X_SERIES", "A100")));
		assertEquals("Unknown CHIP_TYPE value 'A100'; allowed: X_SERIES, Z_SERIES, Q_SERIES", ex.getMessage());

		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.PROCESS_TYPE, Operator.EQUALS, List.of("standard")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.PROCESS_TYPE, Operator.EQUALS, List.of(" ")));
	}

	@Test
	void rejectsOperatorNotSupportedForAttribute() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.GREATER_THAN, List.of("X_SERIES")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.PROCESS_TYPE, Operator.BETWEEN, List.of("LEGACY", "NEXTGEN")));
	}

	@Test
	void rejectsWrongOperandCount() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of()));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, List.of("X_SERIES", "Z_SERIES")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of()));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.NOT_IN, List.of()));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of("1")));
	}

	@Test
	void rejectsInvalidNumericOperands() {
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.EQUALS, List.of("many")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.WAFER_COUNT, Operator.BETWEEN, List.of("30", "10")));
	}

	@Test
	void rejectsNullArguments() {
		assertThrows(InvalidAssignmentRuleException.class, () -> Condition.of(null, Operator.EQUALS, List.of("X_SERIES")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, null, List.of("X_SERIES")));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, null));
		assertThrows(InvalidAssignmentRuleException.class,
				() -> Condition.of(LotAttribute.CHIP_TYPE, Operator.EQUALS, Arrays.asList((String) null)));
	}

	@Test
	void conditionsWithSameDefinitionAreEqual() {
		Condition first = Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("X_SERIES", "Z_SERIES"));
		Condition second = Condition.of(LotAttribute.CHIP_TYPE, Operator.IN, List.of("X_SERIES", "Z_SERIES"));

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertTrue(first.matches(new Lot(ChipType.Z_SERIES, ProcessType.LEGACY, 1)));
		assertFalse(first.matches(new Lot(ChipType.Q_SERIES, ProcessType.LEGACY, 1)));
	}

}
