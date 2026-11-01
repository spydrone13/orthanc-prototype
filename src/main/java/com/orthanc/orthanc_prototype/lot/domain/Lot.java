package com.orthanc.orthanc_prototype.lot.domain;

import java.util.Objects;

public class Lot {

	private final String chipType;
	private final String processType;
	private int waferCount;

	public Lot(String chipType, String processType, int waferCount) {
		this.chipType = requireNonBlank(chipType, "chipType");
		this.processType = requireNonBlank(processType, "processType");
		setWaferCount(waferCount);
	}

	public String getChipType() {
		return chipType;
	}

	public String getProcessType() {
		return processType;
	}

	public int getWaferCount() {
		return waferCount;
	}

	public void setWaferCount(int waferCount) {
		if (waferCount < 0) {
			throw new IllegalArgumentException("waferCount must be >= 0, was " + waferCount);
		}
		this.waferCount = waferCount;
	}

	@Override
	public String toString() {
		return "Lot{chipType='" + chipType + "', processType='" + processType + "', waferCount=" + waferCount + "}";
	}

	private static String requireNonBlank(String value, String name) {
		Objects.requireNonNull(value, name + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
	}

}
