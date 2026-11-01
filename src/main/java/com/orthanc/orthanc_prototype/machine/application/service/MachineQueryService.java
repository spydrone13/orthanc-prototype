package com.orthanc.orthanc_prototype.machine.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.orthanc.orthanc_prototype.machine.application.port.in.ListMachinesUseCase;
import com.orthanc.orthanc_prototype.machine.application.port.out.MachineRepository;
import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

@Service
public class MachineQueryService implements ListMachinesUseCase {

	private final MachineRepository machines;

	public MachineQueryService(MachineRepository machines) {
		this.machines = machines;
	}

	@Override
	public List<EtchingMachine> listAll() {
		return machines.findAll();
	}

}
