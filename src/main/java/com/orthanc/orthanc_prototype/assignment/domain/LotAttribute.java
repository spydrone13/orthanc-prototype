package com.orthanc.orthanc_prototype.assignment.domain;

import java.util.function.Function;

import com.orthanc.orthanc_prototype.lot.domain.Lot;

/**
 * A property of a {@link Lot} that an assignment rule condition can test.
 */
public enum LotAttribute {

	CHIP_TYPE(Kind.TEXT, Lot::getChipType),
	PROCESS_TYPE(Kind.TEXT, Lot::getProcessType),
	WAFER_COUNT(Kind.NUMERIC, Lot::getWaferCount);

	public enum Kind {
		TEXT, NUMERIC
	}

	private final Kind kind;
	private final Function<Lot, Object> accessor;

	LotAttribute(Kind kind, Function<Lot, Object> accessor) {
		this.kind = kind;
		this.accessor = accessor;
	}

	public Kind kind() {
		return kind;
	}

	String textValueOf(Lot lot) {
		return (String) accessor.apply(lot);
	}

	int numericValueOf(Lot lot) {
		return (Integer) accessor.apply(lot);
	}

}
