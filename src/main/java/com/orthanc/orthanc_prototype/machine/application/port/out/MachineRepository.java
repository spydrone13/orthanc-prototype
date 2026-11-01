package com.orthanc.orthanc_prototype.machine.application.port.out;

import java.util.List;
import java.util.Optional;

import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

public interface MachineRepository {

	/** All machines ordered by id. */
	List<EtchingMachine> findAll();

	Optional<EtchingMachine> findById(String id);

	boolean existsById(String id);

	void save(EtchingMachine machine);

}
