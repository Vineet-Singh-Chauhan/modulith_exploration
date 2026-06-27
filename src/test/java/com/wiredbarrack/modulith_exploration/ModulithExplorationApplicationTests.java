package com.wiredbarrack.modulith_exploration;

import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
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

	@Test
	void checkEventListenerForOrderNotification(){
		var modules = ApplicationModules.of(ModulithExplorationApplication.class);

		var ordersModule        = modules.getModuleByName("orders").orElseThrow();
		var notificationsModule = modules.getModuleByName("notifications").orElseThrow();

		// 1. OrderPlaced must be in the orders::dto named interface (part of public API)
		boolean orderPlacedIsPublic = ordersModule.getNamedInterfaces()
				.stream()
				.filter(ni -> "dto".equals(ni.getName()))
				.anyMatch(ni -> ni.contains(OrderPlaced.class));

		org.junit.jupiter.api.Assertions.assertTrue(
				orderPlacedIsPublic,
				"OrderPlaced must be in the orders::dto named interface to be consumable by other modules"
		);

		// 2. notifications module must listen to at least one event from the orders package
		var eventsListenedTo = notificationsModule.getEventsListenedTo(modules);

		boolean listensToOrderEvent = eventsListenedTo.stream()
				.anyMatch(e -> e.getPackageName()
						.startsWith("com.wiredbarrack.modulith_exploration.orders"));

		org.junit.jupiter.api.Assertions.assertTrue(
				listensToOrderEvent,
				"notifications module must have an @ApplicationModuleListener for an orders event, " +
				"but currently listens to: " + eventsListenedTo
		);
	}
}
