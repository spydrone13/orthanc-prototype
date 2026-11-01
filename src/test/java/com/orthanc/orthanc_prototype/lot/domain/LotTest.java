package com.orthanc.orthanc_prototype.lot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LotTest {

	@Test
	void constructorSetsFields() {
		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

		assertEquals(ChipType.X_SERIES, lot.getChipType());
		assertEquals(ProcessType.STANDARD, lot.getProcessType());
		assertEquals(25, lot.getWaferCount());
	}

	@Test
	void setWaferCountUpdatesCount() {
		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

		lot.setWaferCount(23);

		assertEquals(23, lot.getWaferCount());
	}

	@Test
	void rejectsNegativeWaferCount() {
		assertThrows(IllegalArgumentException.class, () -> new Lot(ChipType.X_SERIES, ProcessType.STANDARD, -1));

		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);
		assertThrows(IllegalArgumentException.class, () -> lot.setWaferCount(-1));
	}

	@Test
	void rejectsNullTypes() {
		assertThrows(NullPointerException.class, () -> new Lot(null, ProcessType.STANDARD, 25));
		assertThrows(NullPointerException.class, () -> new Lot(ChipType.X_SERIES, null, 25));
	}

}
