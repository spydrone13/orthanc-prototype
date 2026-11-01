package com.orthanc.orthanc_prototype.lot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LotTest {

	@Test
	void constructorSetsFields() {
		Lot lot = new Lot("A100", "CMOS-7nm", 25);

		assertEquals("A100", lot.getChipType());
		assertEquals("CMOS-7nm", lot.getProcessType());
		assertEquals(25, lot.getWaferCount());
	}

	@Test
	void setWaferCountUpdatesCount() {
		Lot lot = new Lot("A100", "CMOS-7nm", 25);

		lot.setWaferCount(23);

		assertEquals(23, lot.getWaferCount());
	}

	@Test
	void rejectsNegativeWaferCount() {
		assertThrows(IllegalArgumentException.class, () -> new Lot("A100", "CMOS-7nm", -1));

		Lot lot = new Lot("A100", "CMOS-7nm", 25);
		assertThrows(IllegalArgumentException.class, () -> lot.setWaferCount(-1));
	}

	@Test
	void rejectsNullOrBlankTypes() {
		assertThrows(NullPointerException.class, () -> new Lot(null, "CMOS-7nm", 25));
		assertThrows(NullPointerException.class, () -> new Lot("A100", null, 25));
		assertThrows(IllegalArgumentException.class, () -> new Lot(" ", "CMOS-7nm", 25));
		assertThrows(IllegalArgumentException.class, () -> new Lot("A100", "", 25));
	}

}
