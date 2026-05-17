package com.wiredbarrack.modulith_exploration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class ModulithExplorationApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void listModules(){
		var modules = ApplicationModules.of(ModulithExplorationApplication.class);
		modules.forEach(System.out::println);
	}

	@Test
	void checkModularity(){
		ApplicationModules.of(ModulithExplorationApplication.class).verify();
	}
}
