package com.orthanc.orthanc_prototype.machine.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orthanc.orthanc_prototype.machine.application.port.in.ListMachinesUseCase;

@RestController
class MachineController {

	private final ListMachinesUseCase listMachines;

	MachineController(ListMachinesUseCase listMachines) {
		this.listMachines = listMachines;
	}

	@GetMapping("/api/machines")
	List<MachineResponse> list() {
		return listMachines.listAll().stream()
			.map(machine -> new MachineResponse(machine.getId(), machine.getName()))
			.toList();
	}

	record MachineResponse(String id, String name) {
	}

}
