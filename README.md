# Exploring Events In Spring Modulith 

In our last article, we discussed the basics of spring modulith and at this point we know that spring modulith does not magically turns our monolith into a modular monolith but instead provide a set of tests that help us to track places where we violate the modular design patterns and provide a set of tools that help us to reduce coupling and increase cohesion in our application, And one such tool is - Events.

But before we dive straight into events lets first complete a few basic concepts that we did not cover in our last article, this will help us to harden our foundation and have a quick refresher through our last position on code.

# Left over topics:
0. Named Interfaces
1. Open Application Modules
2. Defining Explicit Application Module Dependencies
3. Customizing Module Detection Strategy
4. Customizing Named Interface Detection

So, before diving into the article, I have refactored the code to have proper exceptions and a generic exception.
Also, services has been replaced by interfaces and their implementation has been moved to internal packages. We made all classes except the service interfaces as package private to adhere to intended architecture

One more improvement, I did is that we have a public record per domain for cross domain communication while we keep our DB entity private to the domain .
At this point all our tests pass!
![img.png](img.png)

```

.
├── main
│   ├── java
│   │   └── com
│   │       └── wiredbarrack
│   │           └── modulith_exploration
│   │               ├── inventory
│   │               │   ├── internal
│   │               │   │   ├── InventoryDataSeeder.java
│   │               │   │   ├── InventoryEntity.java
│   │               │   │   ├── InventoryMapper.java
│   │               │   │   ├── InventoryRepository.java
│   │               │   │   └── InventoryServiceImpl.java
│   │               │   ├── Inventory.java
│   │               │   └── InventoryService.java
│   │               ├── ModulithExplorationApplication.java
│   │               ├── notifications
│   │               │   ├── internal
│   │               │   │   ├── NotificationDataSeeder.java
│   │               │   │   ├── NotificationEntity.java
│   │               │   │   ├── NotificationMapper.java
│   │               │   │   ├── NotificationRepository.java
│   │               │   │   └── NotificationServiceImpl.java
│   │               │   ├── Notification.java
│   │               │   └── NotificationService.java
│   │               ├── orders
│   │               │   ├── internal
│   │               │   │   ├── OrderDataSeeder.java
│   │               │   │   ├── OrderEntity.java
│   │               │   │   ├── OrderMapper.java
│   │               │   │   ├── OrderRepository.java
│   │               │   │   └── OrderSeviceImpl.java
│   │               │   ├── Order.java
│   │               │   └── OrderSevice.java
│   │               ├── users
│   │               │   ├── internal
│   │               │   │   ├── UserDataSeeder.java
│   │               │   │   ├── UserEntity.java
│   │               │   │   ├── UserMapper.java
│   │               │   │   ├── UserRepository.java
│   │               │   │   └── UserServiceImpl.java
│   │               │   ├── User.java
│   │               │   └── UserService.java
│   │               └── utility
│   │                   └── DatabaseBackupUtility.java
│   └── resources
│       ├── application.yml
│       ├── backup.sql
│       ├── static
│       └── templates
└── test
    └── java
        └── com
            └── wiredbarrack
                └── modulith_exploration
                    └── ModulithExplorationApplicationTests.java
```
This works fine if we have to expose a few DTOs/records per domain, but if a domain somehow needs to expose more than few DTOs we would like to have a separate package for the dtos
but once we move it to that package, it would not be part of public api anymore

That's where Named Interfaces come to our rescue,
With the help of Named interfaces, we can define some sub-packages as public and thus part of public API

Lets create a subpackage `dto` for each of our domains and move our records there.

```

├── main
│   ├── java
│   │   └── com
│   │       └── wiredbarrack
│   │           └── modulith_exploration
│   │               ├── inventory
│   │               │   ├── dto
│   │               │   │   └── InventoryService.java
│   │               │   ├── internal
│   │               │   │   ├── InventoryDataSeeder.java
│   │               │   │   ├── InventoryEntity.java
│   │               │   │   ├── InventoryMapper.java
│   │               │   │   ├── InventoryRepository.java
│   │               │   │   └── InventoryServiceImpl.java
│   │               │   └── Inventory.java
│   │               ├── ModulithExplorationApplication.java
│   │               ├── notifications
│   │               │   ├── dto
│   │               │   │   └── Notification.java
│   │               │   ├── internal
│   │               │   │   ├── NotificationDataSeeder.java
│   │               │   │   ├── NotificationEntity.java
│   │               │   │   ├── NotificationMapper.java
│   │               │   │   ├── NotificationRepository.java
│   │               │   │   └── NotificationServiceImpl.java
│   │               │   └── NotificationService.java
│   │               ├── orders
│   │               │   ├── dto
│   │               │   │   └── Order.java
│   │               │   ├── internal
│   │               │   │   ├── OrderDataSeeder.java
│   │               │   │   ├── OrderEntity.java
│   │               │   │   ├── OrderMapper.java
│   │               │   │   ├── OrderRepository.java
│   │               │   │   └── OrderSeviceImpl.java
│   │               │   └── OrderSevice.java
│   │               ├── users
│   │               │   ├── dto
│   │               │   │   └── User.java
│   │               │   ├── internal
│   │               │   │   ├── UserDataSeeder.java
│   │               │   │   ├── UserEntity.java
│   │               │   │   ├── UserMapper.java
│   │               │   │   ├── UserRepository.java
│   │               │   │   └── UserServiceImpl.java
│   │               │   └── UserService.java
│   │               └── utility
│   │                   └── DatabaseBackupUtility.java
│   └── resources
│       ├── application.yml
│       ├── backup.sql
│       ├── static
│       └── templates
└── test
    └── java
        └── com
            └── wiredbarrack
                └── modulith_exploration
                    └── ModulithExplorationApplicationTests.java
```
 
I have started to consume the `Inventory` record in `placeOrder` method of `OrderServiceImpl` class, but I am not able to use it in the `InventoryRepository` class because it is in a different package and thus not part of public API.
```
        Inventory inventory = inventoryService.acquireInventoryItem(order.id(), order.itemCount());
        log.info("Inventory updated for item id : {} with count : {}", inventory.id(), inventory.count());
```
Note: if we don't call any of the methods of `Inventory` record in `OrderServiceImpl` class, then it will not be a problem and modularity test will pass, but as soon as we call any of the methods of `Inventory` record in `OrderServiceImpl` class, it will fail because `Inventory` record is not part of public API anymore.
So our Modularity test shall fail:

```
org.springframework.modulith.core.Violations: - Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.dto.Inventory within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.internal.OrderSeviceImpl.placeOrder(com.wiredbarrack.modulith_exploration.orders.dto.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.dto.Inventory.id()> in (OrderSeviceImpl.java:43)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.dto.Inventory within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.internal.OrderSeviceImpl.placeOrder(com.wiredbarrack.modulith_exploration.orders.dto.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.dto.Inventory.count()> in (OrderSeviceImpl.java:43)

	at org.springframework.modulith.core.Violations.and(Violations.java:144)
	at java.base/java.util.stream.ReduceOps$1ReducingSink.accept(ReduceOps.java:80)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.HashMap$ValueSpliterator.forEachRemaining(HashMap.java:1787)
	at java.base/java.util.stream.Streams$ConcatSpliterator.forEachRemaining(Streams.java:735)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:921)
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.base/java.util.stream.ReferencePipeline.reduce(ReferencePipeline.java:657)
	at org.springframework.modulith.core.ApplicationModules.detectViolations(ApplicationModules.java:475)
	at org.springframework.modulith.core.ApplicationModules.verify(ApplicationModules.java:440)
	at com.wiredbarrack.modulith_exploration.ModulithExplorationApplicationTests.checkModularity(ModulithExplorationApplicationTests.java:23)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
```
To overcome this problem, we use named interfaces to expose the `Inventory` record to other modules.
Thus, we add `package-info.java` file in the `inventory.dto` package and expose the `Inventory` record to other modules using named interfaces.

```
@org.springframework.modulith.NamedInterface("dto")
package com.wiredbarrack.modulith_exploration.inventory.dto;
```
Now, our tests pass:

![img_1.png](img_1.png)

Also, to strictly define the allowed dependencies for a module we can add `@org.springframework.modulith.AllowedDependencies` annotation to the `package-info.java` file of a module. For example:
```
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"inventory", "inventory::dto", "notifications"}
)
```

Still we have an problem, that our `OrderService` has an hard dependency on `NotificationService`(High Coupling), this has two-fold issues: 1. that application imports it causing high coupling, 2. It hampers independent testing of `OrderService`, as we need to mock it and any time NotifcationService changes, we need to touch every module that uses it.
It also means that we will have to touch the class whenever we would like to integrate further functionality with the business event order completion.

To overcome this problem, we can use the `ApplicationEvents` already provided by Modulith to decouple services


Step 1: Create a new event class `OrderPlacedEvent` in the `orders` module, which will be published when an order is placed.
```java
@Getter
@Builder
public class OrderPlaced {
    private Integer id;
}
```

Step 2: In the `OrderServiceImpl` class, publish the `OrderPlacedEvent` when an order is placed.
```java

private final ApplicationEventPublisher events;

...
log.info("Order Placed with id : {}", orderEntity.getId());
events.publishEvent(OrderPlaced.builder().id(orderEntity.getId()).build());
return mapper.toRecord(orderEntity);
...
```
Step 3: In the `notifications` package, listen for the `OrderPlacedEvent` and send a notification when the event is received.
```java
@Slf4j
@Component
@RequiredArgsConstructor
class OrderEventListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    void onOrderPlaced(OrderPlaced event) {
        log.info("Received OrderPlaced event for order id: {}", event.getId());
        notificationService.saveNotification("Your order has been placed successfully!");
    }
}

```

I have added following tests to verify the working of the above implementation.

```java

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
```

and
 ```java

@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
class NotificationsModuleTests {

    @Test
    void checkEventListenerForOrderNotification(Scenario scenario) {

        var event = OrderPlaced.builder().id(42).build();

        scenario.publish(event)
                .andWaitForStateChange(() -> event);
    }
}
 ```

### Durable Events:
Now, consider a scenario where the `notifications` module is down when the `OrderPlaced` event is published. In this case, the event will be lost and the notification will not be sent. To overcome this problem, we can use durable events. Durable events are persisted in the database and can be replayed when the module is back up.
So, spring modulith maintains a table `event_publication` in the database to store the events. When a module is down, the events are stored in this table and when the module is back up, the events are replayed.



Automatic re-publication of the events can be enabled via the `spring.modulith.events.republish-outstanding-events-on-restart property`.

To mimic this flow, lets deliberately add exception to notification service and check if event is stored with which state in `event_publication` table.
















    @Query("SELECT new com.wiredbarrack.modulith_exploration.inventory.dto.Inventory(i.id,i.name,i.category,i.count) FROM product p where p.id = :id ")
    Optional<Inventory> findProductById(Integer id);


