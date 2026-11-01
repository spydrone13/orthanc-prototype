package com.orthanc.orthanc_prototype.machine.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.orthanc.orthanc_prototype.lot.domain.ChipType;
import com.orthanc.orthanc_prototype.lot.domain.Lot;
import com.orthanc.orthanc_prototype.lot.domain.ProcessType;

class EtchingMachineTest {

	@Test
	void constructorSetsFields() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");

		assertEquals("ETCH-01", machine.getId());
		assertEquals("Plasma Etcher 1", machine.getName());
	}

	@Test
	void rejectsNullOrBlankFields() {
		assertThrows(NullPointerException.class, () -> new EtchingMachine(null, "Plasma Etcher 1"));
		assertThrows(NullPointerException.class, () -> new EtchingMachine("ETCH-01", null));
		assertThrows(IllegalArgumentException.class, () -> new EtchingMachine(" ", "Plasma Etcher 1"));
		assertThrows(IllegalArgumentException.class, () -> new EtchingMachine("ETCH-01", ""));
	}

	@Test
	void newMachineHasNoAssignedLots() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");

		assertTrue(machine.getAssignedLots().isEmpty());
	}

	@Test
	void assignLotKeepsLotsInAssignmentOrder() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");
		Lot first = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);
		Lot second = new Lot(ChipType.Z_SERIES, ProcessType.NEXTGEN, 20);

		machine.assignLot(first);
		machine.assignLot(second);

		assertEquals(List.of(first, second), machine.getAssignedLots());
	}

	@Test
	void rejectsAssigningSameLotTwice() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");
		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);
		machine.assignLot(lot);

		assertThrows(IllegalStateException.class, () -> machine.assignLot(lot));
		assertEquals(List.of(lot), machine.getAssignedLots());
	}

	@Test
	void rejectsAssigningNullLot() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");

		assertThrows(NullPointerException.class, () -> machine.assignLot(null));
	}

	@Test
	void unassignLotRemovesOnlyThatLot() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");
		Lot first = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);
		Lot second = new Lot(ChipType.Z_SERIES, ProcessType.NEXTGEN, 20);
		machine.assignLot(first);
		machine.assignLot(second);

		machine.unassignLot(first);

		assertEquals(List.of(second), machine.getAssignedLots());
	}

	@Test
	void rejectsUnassigningLotThatIsNotAssigned() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");
		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

		assertThrows(IllegalArgumentException.class, () -> machine.unassignLot(lot));
	}

	@Test
	void assignedLotsCannotBeModifiedDirectly() {
		EtchingMachine machine = new EtchingMachine("ETCH-01", "Plasma Etcher 1");
		Lot lot = new Lot(ChipType.X_SERIES, ProcessType.STANDARD, 25);

		assertThrows(UnsupportedOperationException.class, () -> machine.getAssignedLots().add(lot));
	}

}
