package com.orthanc.orthanc_prototype.lot.domain;

import java.util.Objects;

public class Lot {

	private final ChipType chipType;
	private final ProcessType processType;
	private int waferCount;

	public Lot(ChipType chipType, ProcessType processType, int waferCount) {
		this.chipType = Objects.requireNonNull(chipType, "chipType must not be null");
		this.processType = Objects.requireNonNull(processType, "processType must not be null");
		setWaferCount(waferCount);
	}

	public ChipType getChipType() {
		return chipType;
	}

	public ProcessType getProcessType() {
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
		return "Lot{chipType=" + chipType + ", processType=" + processType + ", waferCount=" + waferCount + "}";
	}

}
