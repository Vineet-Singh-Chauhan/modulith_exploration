# Introduction

This project is aimed to explore spring modulith, on how can we integrate it in our projects, the feature that it provides and how to use them.

## What is a 'Modulith'?
To answer this we need to know two terms before hand:
1. monolith: it is a single big application having code to handle all the responsibilities within itself.
2. modular: it is an adjective that defines that a thing is made up of smaller pieces (modules) that can be changed if needed without affecting other parts (other modules) of the system.

Now if a monolith is modular that can be called a modular-monolith or a 'modulith', I guess this was the idea of Oliver Drotbohn behind naming this project modulith.

So Basically there can be two approaches of structuring an application that defines to two far ends of the road:
1. Monolithic Architeture: this visions a appliction as single large piece of software having all responsibilities in it
2. Microservices: The system consists of various separate applications each responsible for its own business that it solves. And System relies on communication between them

And we have 'modulith' approach as the mid-point of these two - a monolith application that internally have clear separation of concerns between modules. And thus achieving high cohesion and low coupling.

// TODO: Explain cohesion and coupling here and what is desirable in software and why. add a funny graph as well after if

So here we are going to build a demo newsagency backend on spring-modulith to understand the features it provides and architectural opinion that it provides.

## Overview:
Here is the overview that we are going to build, a dummy ecommerce service -:
We can structure the project in horizontal slices as:
src
|-controllers               // this holds the entry points - we keep controllers thin ( standard practice )
|--OrderController
|--UserController
|--InventoryController
|-Services                  // We keep services thick ( all business logic goes here)
|--OrderService
|--UserService
|--InventoryService
|--NotificationService
|-repository
|--UserRepository
|--OrderRepository
|--InventoryRepository
|--NotificationRepository
Now this is a MVC pattern architecture that structures project in horizontal slices ( basis infrastructure) Each slice is a part of infra and performs its job - controllers are entry points, they route, services are business brains, and repositories are persistence layer.
This might work well for smaller projects but as the project grows we have a ton of interservice communications- synchronous and asycn both.
Overtime, Notification service might be directly calling OrderRepository to fetch delivered orders to issue notifications. NotificationRepository might be querying orders tables etc.
Later this causes the refactoring code dangerous as that can have impact on multiple services. There is no clear separation of business 'domains' a service is expected to adhere to!

So DDD or Domain Driven Design says, structure your projects basis domains ( verical slices ) and not infra ( horizontal slices ).
We will have following folder structure:
src
|-orders
|--OrderController
|--OrderService
|--OrderRepository
|-users
|--UserController
|--UserService
|--UserRepository
|-inventory
|--InventoryController
|--InventoryService
|--InventoryRepository
|-notifications
|--NotificationService
|--NotificationRepository

This helps to structure code that defines domains each service and repositoru shall confine to. (high cohesiveness and low coupling).
But this do not enforces the developers to break the boundaries , they can still make NotificationService to call OrderRepository or to directly query Orders tables.

We can also make Repositories package private to escape this a bit but it will break as we have nested structures in our domains (as java nested packages are considered as separate packages altogether and cannot access methods even if they reside in the same parent package).


This is where spring-modulith shines. It  suggests to have each domain some internal implementations not allowed to refered by other domains, and a public api that exposes methods (controlled) that can be refered bby other services.
Spring Modulith also ships with ApplicationModuleTest that helps you to identify the classes that break the domain boundaries, thus enforcing developers to respect the boundaries. And keeping the project modular in long term.

Modulith is not limited to above domain segregation alone, it gives waay cross domain communicaton will work- through application events,Spring Modulith allows to run integration tests bootstrapping individual application modules in isolation or combination with others., Moments - passsage of time api, durable events, documentation support and module level observablity apis etc that we will discuss in this series of articles - one at a time.

So now lets jump into our dummy project to understand how we can implement all this that we discussed.

so lets first start by adding spring-modulith to our dependencies
Spring Modulith consists of a set of libraries that can be used individually and depending on which features of it you would like to use. To ease the declaration of the individual modules, we recommend to declare the following BOM in your Maven POM:
```
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.modulith</groupId>
      <artifactId>spring-modulith-bom</artifactId>
      <version>2.0.6</version>
      <scope>import</scope>
      <type>pom</type>
    </dependency>
  </dependencies>
</dependencyManagement>
```
For now we will be using spring modulith starter core, jpa, and tests:
```
	<dependency>
			<groupId>org.springframework.modulith</groupId>
			<artifactId>spring-modulith-starter-core</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.modulith</groupId>
			<artifactId>spring-modulith-starter-jpa</artifactId>
		</dependency>
        <dependency>
			<groupId>org.springframework.modulith</groupId>
			<artifactId>spring-modulith-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
```

I am using h2 db for this:
```
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
```
// TODO: explain each dependency here.

So our pom currently look like this:
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.2.5</version>
		<relativePath/>
	</parent>

	<groupId>com.wiredbarrack</groupId>
	<artifactId>newsagency</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>The Wired Chronicles News Agency</name>
	<description>A Modular Monolith Tutorial Project</description>

	<properties>
		<java.version>21</java.version>
		<spring-modulith.version>1.2.0</spring-modulith.version>
	</properties>

	<dependencies>
		<!-- Core Web Support -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<!-- Spring Modulith Starters -->
		<dependency>
			<groupId>org.springframework.modulith</groupId>
			<artifactId>spring-modulith-starter-core</artifactId>
		</dependency>

        <dependency>
           <groupId>org.springframework.modulith</groupId>
           <artifactId>spring-modulith-starter-jpa</artifactId>
        </dependency>

		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.modulith</groupId>
			<artifactId>spring-modulith-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.modulith</groupId>
				<artifactId>spring-modulith-bom</artifactId>
				<version>${spring-modulith.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```
I got following error at startup:


```


***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of method jpaEventPublicationRepository in org.springframework.modulith.events.jpa.JpaEventPublicationConfiguration required a bean of type 'jakarta.persistence.EntityManager' that could not be found.


Action:

Consider defining a bean of type 'jakarta.persistence.EntityManager' in your configuration.


Process finished with exit code 1
```
Reason:
The most likely reason for this is that your Maven project hasn't successfully downloaded the JPA dependencies, or they are missing from the pom.xml.

In a Spring Boot project, the spring-boot-starter-data-jpa is the "parent" dependency that brings in jakarta.persistence. Ensure your pom.xml includes this:
XML

<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

(Note: Even though you have the Modulith JPA starter, adding the standard Boot JPA starter explicitly often resolves IDE resolution issues.)

Also i have added following properties in my application.yml:
spring:
    h2:
        console:
        enabled: true
        path: /h2-console
        settings:
            web-allow-others: true # Optional: allow remote access
        datasource:
            generate-unique-name: false # This ensures the name stays 'ecomm_db' and doesn't get a random suffix
 this helps us to view h2console and tables in web browsers
http://localhost:8080/h2-console/

## Phase 01: Understanding boundaries
At this commit: 83c1cae334684cd9928046619bb0fe861e3200af
We have files that are divided into packages basis domains but if you notice OrderService and it imports :
```

package com.wiredbarrack.modulith_exploration.orders;

import com.wiredbarrack.modulith_exploration.inventory.internal.Inventory;
import com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository;
import com.wiredbarrack.modulith_exploration.notifications.internal.Notification;
import com.wiredbarrack.modulith_exploration.notifications.NotificationService;

```

There are no boundaries enforced! order domain is essentially querying Inventory table directly and has a hard dependency upon Notification (Don't mind synchronous nature of notification, you its just for showcasing the use case!)

But as you may argue that we can make things package private and then expose domain services to talk! and you are right but to some extent!

At this commit: ef4e9a4eeac348d5d915a799d11a42583e86b415

We have every domain having their respective service at base package level and all the repository at `basePackage.internal`;
Thus, we are saying use services as interface to communicate, but you cannot enforce it - why as in java nested packages are considered as separate packages altogether and cannot access methods even if they reside in the same parent package, so you have to make them public and once everything is public no boundaries are enforced
But But, if you see the our modularity test will fail now!:

Screenshot of list Modules:
Check how the classes at base package level makes public API and nested classes and packages are enforced to be private (this also explains why our modularity test used to pass earlier).
```

# Utility
> Logical name: utility
> Base package: com.wiredbarrack.modulith_exploration.utility
> Spring beans:
  + ….DatabaseBackupUtility

# Orders
> Logical name: orders
> Base package: com.wiredbarrack.modulith_exploration.orders
> Spring beans:
  + ….OrderSevice
  o ….internal.OrderDataSeeder
  o ….internal.OrderRepository

# Inventory
> Logical name: inventory
> Base package: com.wiredbarrack.modulith_exploration.inventory
> Spring beans:
  + ….InventoryService
  o ….internal.InventoryDataSeeder
  o ….internal.InventoryRepository

# Notifications
> Logical name: notifications
> Base package: com.wiredbarrack.modulith_exploration.notifications
> Spring beans:
  + ….NotificationService
  o ….internal.NotificationDataSeeder
  o ….internal.NotificationRepository

# Users
> Logical name: users
> Base package: com.wiredbarrack.modulith_exploration.users
> Spring beans:
  + ….UserService
  o ….internal.UserDataSeeder
  o ….internal.UserRepository

```
Check Modularity Test:

```
org.springframework.modulith.core.Violations: - Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.Inventory within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.internal.Inventory.getCount()> in (OrderSevice.java:35)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.Inventory within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.internal.Inventory.setCount(java.lang.Integer)> in (OrderSevice.java:35)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository.save(java.lang.Object)> in (OrderSevice.java:36)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository.findById(java.lang.Object)> in (OrderSevice.java:31)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.notifications.internal.Notification$NotificationBuilder within module 'notifications'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.notifications.internal.Notification$NotificationBuilder.build()> in (OrderSevice.java:40)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.Inventory within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.internal.Inventory.getCount()> in (OrderSevice.java:32)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.notifications.internal.Notification within module 'notifications'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.notifications.internal.Notification.builder()> in (OrderSevice.java:40)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.notifications.internal.Notification$NotificationBuilder within module 'notifications'!
Method <com.wiredbarrack.modulith_exploration.orders.OrderSevice.placeOrder(com.wiredbarrack.modulith_exploration.orders.internal.Order)> calls method <com.wiredbarrack.modulith_exploration.notifications.internal.Notification$NotificationBuilder.message(java.lang.String)> in (OrderSevice.java:40)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository within module 'inventory'!
Field <com.wiredbarrack.modulith_exploration.orders.OrderSevice.inventoryRepository> has type <com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository> in (OrderSevice.java:0)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.internal.Inventory within module 'inventory'!
Field <com.wiredbarrack.modulith_exploration.orders.internal.Order.itemId> has annotation member of type <com.wiredbarrack.modulith_exploration.inventory.internal.Inventory> in (Order.java:0)

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
	at com.wiredbarrack.modulith_exploration.ModulithExplorationApplicationTests.checkModularity(ModulithExplorationApplicationTests.java:22)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
```

Thus, modulith enforces us to follow domain boundaries!
## Fixing modularity tests:

We have remove direct cross module dependencies and shall expose public APIs through services (in our case).
At this commit : 4829a3550bbe34d1589c03c5391db82c40212675
I have refactored the code to remove cross-domain boundaries
Also note that in Order.java I had mistakenly placed @Reference(to = Inventory.java) that I have removed as We shall not have hard FK links in DB as well to have complete modularity and @Reference was any was wrong for SQL entities.
But we shall have FKs logically enforced in application layer to have truely modularised DB.
Architectural Takeaway: Hard JPA mapping is for consistency within a single domain boundary. Loose ID reference is for scalability and isolation across domain boundaries.
For Example:

```
package com.wiredbarrack.modulith_exploration.orders.internal;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Order {
    @Id @GeneratedValue 
    private Integer id;

    // Hard mapping is PERFECT here because both classes live in the same module
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderLineItem> items;
}
```

```
package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.inventory.InventoryDTO;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailFacade {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService; // Public API entrypoint

    public OrderResponse getOrderDetails(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        
        // Fetch the loose dependency explicitly through the public gateway
        InventoryDTO itemDetails = inventoryService.getInventoryItem(order.getItemId());
        
        // Combine them into a single response object for the UI
        return new OrderResponse(order, itemDetails);
    }
}

```


Now, the modularity tests pass!.

## More on  handling FKs:
When you move to a modular architecture, you deliberately drop physical Foreign Key (FK) constraints at the database level between different modules. Instead, you rely on Logical Foreign Keys enforced by your application logic.
The Real-World Reality: Database FKs are a Trap for Modularity

If you put a physical database constraint between the orders table and the inventory table, you have completely defeated the purpose of Spring Modulith.

You have created a Shared Database Anti-Pattern. Even though your Java code looks beautifully decoupled into packages, your database schemas are still tangled together in a tight knot. If you ever want to split inventory into its own microservice or its own database instance later, that physical constraint will completely block you.
How to Handle Data Integrity Without Database FKs

But if there's no FK constraint, what stops someone from creating an order with a productId that doesn't exist?

You shift the responsibility of integrity from the database engine to the application services. You handle this through three defensive layers:
1. Application-Level Validation (The Front Line)

Before an entity is saved, your service layer must validate that the referenced ID is real by querying the target module's public API.
Java
```
package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService; // The public gatekeeper

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Enforce data integrity programmatically BEFORE saving
        boolean itemExists = inventoryService.isValidItem(request.getItemId());
        if (!itemExists) {
            throw new IllegalArgumentException("Cannot place order: Product ID does not exist.");
        }

        // 2. Proceed with saving if valid
        Order order = Order.builder()
            .itemId(request.getItemId())
            .itemCount(request.getItemCount())
            .status("CREATED")
            .build();
            
        return orderRepository.save(order);
    }
}
```

2. Handling Deletions via Events (Event-Driven Integrity)

In a traditional database, you might use ON DELETE CASCADE or ON DELETE RESTRICT. In a modular architecture, you handle deletions asynchronously using Spring Application Events.

If an item is deleted in the inventory module, it fires an internal domain event. The orders module listens for this event and decides how to handle its own data integrity safely:
Java

```
package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryDeletedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final OrderRepository orderRepository;

    @ApplicationModuleListener
    public void onInventoryDeleted(InventoryDeletedEvent event) {
        // Handle the "foreign key deletion" within your own module's context
        // Option A: Cancel pending orders for this item
        // Option B: Archive/Soft-delete records smoothly
        orderRepository.failOrdersForDeletedItem(event.getItemId());
    }
}
```
3. Defending Against In-Flight Changes (The Outbox Pattern)

Because you are using Spring Modulith, it comes bundled with an Event Publication Registry. If the inventory module publishes a change, Modulith saves that event into a special database table (event_publication) within the same transaction. Even if the system crashes mid-operation, Modulith ensures the event is eventually delivered to the orders module, guaranteeing eventual consistency across your logical boundaries.

We will discuss events in detail in next article.

## References:
1. Spring Modulith Documentation:https://docs.spring.io/spring-modulith/reference/index.html
