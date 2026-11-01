package com.orthanc.orthanc_prototype.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.orthanc.orthanc_prototype.lot.Lot;

public class EtchingMachine {

	private final String id;
	private final String name;
	private final List<Lot> assignedLots = new ArrayList<>();

	public EtchingMachine(String id, String name) {
		this.id = requireNonBlank(id, "id");
		this.name = requireNonBlank(name, "name");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Lot> getAssignedLots() {
		return List.copyOf(assignedLots);
	}

	public void assignLot(Lot lot) {
		Objects.requireNonNull(lot, "lot must not be null");
		if (assignedLots.contains(lot)) {
			throw new IllegalStateException("Lot is already assigned to EtchingMachine " + id + ": " + lot);
		}
		assignedLots.add(lot);
	}

	public void unassignLot(Lot lot) {
		if (!assignedLots.remove(lot)) {
			throw new IllegalArgumentException("Lot is not assigned to EtchingMachine " + id + ": " + lot);
		}
	}

	@Override
	public String toString() {
		return "EtchingMachine{id='" + id + "', name='" + name + "', assignedLots=" + assignedLots.size() + "}";
	}

	private static String requireNonBlank(String value, String name) {
		Objects.requireNonNull(value, name + " must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
	}

}
