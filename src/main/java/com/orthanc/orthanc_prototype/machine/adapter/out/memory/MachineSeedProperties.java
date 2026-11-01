package com.orthanc.orthanc_prototype.machine.adapter.out.memory;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orthanc")
public record MachineSeedProperties(List<MachineSeed> machines) {

	public MachineSeedProperties {
		machines = machines == null ? List.of() : List.copyOf(machines);
	}

	public record MachineSeed(String id, String name) {
	}

}
