package com.orthanc.orthanc_prototype.machine.adapter.out.memory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Repository;

import com.orthanc.orthanc_prototype.machine.application.port.out.MachineRepository;
import com.orthanc.orthanc_prototype.machine.domain.EtchingMachine;

@Repository
@EnableConfigurationProperties(MachineSeedProperties.class)
public class InMemoryMachineRepository implements MachineRepository {

	private final Map<String, EtchingMachine> machines = new ConcurrentHashMap<>();

	public InMemoryMachineRepository(MachineSeedProperties seedProperties) {
		seedProperties.machines().forEach(seed -> save(new EtchingMachine(seed.id(), seed.name())));
	}

	@Override
	public List<EtchingMachine> findAll() {
		return machines.values().stream().sorted(Comparator.comparing(EtchingMachine::getId)).toList();
	}

	@Override
	public Optional<EtchingMachine> findById(String id) {
		return Optional.ofNullable(machines.get(id));
	}

	@Override
	public boolean existsById(String id) {
		return machines.containsKey(id);
	}

	@Override
	public void save(EtchingMachine machine) {
		Objects.requireNonNull(machine, "machine must not be null");
		machines.put(machine.getId(), machine);
	}

}
