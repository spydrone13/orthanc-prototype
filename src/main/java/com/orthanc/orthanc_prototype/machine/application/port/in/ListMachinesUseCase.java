package com.orthanc.orthanc_prototype.machine.application.port.in;

import java.util.List;

import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

public interface ListMachinesUseCase {

	/** All machines ordered by id. */
	List<EtchingMachine> listAll();

}
