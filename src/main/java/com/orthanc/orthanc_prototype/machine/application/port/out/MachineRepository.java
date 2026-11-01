package com.orthanc.orthanc_prototype.machine.application.port.out;

import java.util.Optional;

import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

public interface MachineRepository {

	Optional<EtchingMachine> findById(String id);

	boolean existsById(String id);

	void save(EtchingMachine machine);

}
