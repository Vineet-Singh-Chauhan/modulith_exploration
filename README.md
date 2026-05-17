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

## References:
1. Spring Modulith Documentation:https://docs.spring.io/spring-modulith/reference/index.html
