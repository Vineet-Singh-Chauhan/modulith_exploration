package com.wiredbarrack.modulith_exploration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ModulithExplorationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModulithExplorationApplication.class, args);
	}

}
