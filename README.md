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

Our Modularity tests will fail now for above explained reasons:

```
org.springframework.modulith.core.Violations: - Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.InventoryService within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.internal.OrderSeviceImpl.placeOrder(com.wiredbarrack.modulith_exploration.orders.dto.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.InventoryService.acquireInventoryItem(java.lang.Integer, int)> in (OrderSeviceImpl.java:36)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.InventoryService within module 'inventory'!
Method <com.wiredbarrack.modulith_exploration.orders.internal.OrderSeviceImpl.placeOrder(com.wiredbarrack.modulith_exploration.orders.dto.Order)> calls method <com.wiredbarrack.modulith_exploration.inventory.InventoryService.getInventoryCount(java.lang.Integer)> in (OrderSeviceImpl.java:32)
- Module 'orders' depends on non-exposed type com.wiredbarrack.modulith_exploration.inventory.InventoryService within module 'inventory'!
Field <com.wiredbarrack.modulith_exploration.orders.internal.OrderSeviceImpl.inventoryService> has type <com.wiredbarrack.modulith_exploration.inventory.InventoryService> in (OrderSeviceImpl.java:0)

```
So here we make dto packages as Named Interfaces:
For this we need to have a `package-info.java` file at the package we want to declare public
and then annotate it with `NamedInterface` annotation
ex:
```
@org.springframework.modulith.NamedInterface("dto")
package com.wiredbarrack.modulith_exploration.inventory.dto;
```

```

.
├── dto
│   ├── Inventory.java
│   └── package-info.java
└── internal
├── InventoryDataSeeder.java
├── InventoryEntity.java
├── InventoryMapper.java
├── InventoryRepository.java
├── InventoryServiceImpl.java
└── InventoryService.java
```








































    @Query("SELECT new com.wiredbarrack.modulith_exploration.inventory.dto.Inventory(i.id,i.name,i.category,i.count) FROM product p where p.id = :id ")
    Optional<Inventory> findProductById(Integer id);


