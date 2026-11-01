package com.orthanc.orthanc_prototype;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:context-loads")
class OrthancPrototypeApplicationTests {

	@Test
	void contextLoads() {
	}

}
