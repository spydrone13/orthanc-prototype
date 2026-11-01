package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

/**
 * A property of a {@link Lot} that an assignment rule condition can test.
 */
public enum LotAttribute {

	CHIP_TYPE(Kind.ENUMERATED, ChipType.class, Lot::getChipType),
	PROCESS_TYPE(Kind.ENUMERATED, ProcessType.class, Lot::getProcessType),
	WAFER_COUNT(Kind.NUMERIC, null, Lot::getWaferCount);

	public enum Kind {
		/** One of a fixed set of values, see {@link LotAttribute#allowedValues()}. */
		ENUMERATED,
		NUMERIC
	}

	private final Kind kind;
	private final List<String> allowedValues;
	private final Function<Lot, Object> accessor;

	LotAttribute(Kind kind, Class<? extends Enum<?>> valueType, Function<Lot, Object> accessor) {
		this.kind = kind;
		this.allowedValues = valueType == null ? List.of()
				: Arrays.stream(valueType.getEnumConstants()).map(Enum::name).toList();
		this.accessor = accessor;
	}

	public Kind kind() {
		return kind;
	}

	/** The permitted operand values for {@link Kind#ENUMERATED} attributes; empty otherwise. */
	public List<String> allowedValues() {
		return allowedValues;
	}

	String enumValueOf(Lot lot) {
		return ((Enum<?>) accessor.apply(lot)).name();
	}

	int numericValueOf(Lot lot) {
		return (Integer) accessor.apply(lot);
	}

}
