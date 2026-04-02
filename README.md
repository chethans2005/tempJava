# Quick Commerce Grocery Delivery Application

A fully object-oriented Java project modelled from the UML class diagram.

---

## Project Structure

```
QuickCommerceApp/
└── src/main/java/com/quickcommerce/
    ├── Main.java                          ← Entry point / demo runner
    │
    ├── enums/
    │   ├── OrderStatus.java               ← PENDING, CONFIRMED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    │   ├── PaymentMethod.java             ← UPI, CREDIT_CARD, DEBIT_CARD, COD, etc.
    │   └── PaymentStatus.java             ← PENDING, SUCCESS, FAILED, REFUNDED
    │
    ├── exception/
    │   ├── ResourceNotFoundException.java ← Thrown when an entity is not found
    │   ├── InsufficientStockException.java← Thrown when stock cannot fulfil a request
    │   └── InvalidOperationException.java ← Thrown for illegal state transitions
    │
    ├── interfaces/
    │   ├── Trackable.java                 ← Contract for trackable entities (ISP)
    │   └── Processable.java               ← Contract for processable entities (ISP)
    │
    ├── model/
    │   ├── User.java                      ← Abstract base class (Inheritance root)
    │   ├── Customer.java                  ← Extends User; owns Cart and Orders
    │   ├── DeliveryPartner.java           ← Extends User; handles deliveries
    │   ├── Vendor.java                    ← Extends User; manages product catalogue
    │   ├── Administrator.java             ← Extends User; platform admin
    │   ├── Product.java                   ← A product listing
    │   ├── Cart.java                      ← Shopping cart (1-to-1 with Customer)
    │   ├── CartItem.java                  ← Line item inside a Cart
    │   ├── Order.java                     ← A placed order (implements Trackable)
    │   ├── OrderItem.java                 ← Immutable line item inside an Order
    │   └── Payment.java                   ← Payment transaction (implements Processable)
    │
    └── service/
        ├── UserService.java               ← User registry, registration, auth
        ├── ProductService.java            ← Platform-wide product catalogue
        └── OrderService.java              ← Order lifecycle, delivery assignment
```

---

## OOP Principles Applied

| Principle | Where |
|---|---|
| **Abstraction** | `User` is abstract; `Trackable` and `Processable` are interfaces |
| **Encapsulation** | All fields are private; accessed only via getters/setters |
| **Inheritance** | `Customer`, `Vendor`, `DeliveryPartner`, `Administrator` all extend `User` |
| **Polymorphism** | `getRole()` overridden in every subclass; `Trackable` and `Processable` used generically |
| **Single Responsibility (SRP)** | Each class has one reason to change; service layer handles orchestration |
| **Open/Closed (OCP)** | New user types extend `User` without modifying it |
| **Liskov Substitution (LSP)** | Every `User` subtype can be used wherever `User` is expected |
| **Interface Segregation (ISP)** | `Trackable` and `Processable` are small, focused interfaces |
| **Dependency Inversion (DIP)** | Services depend on abstractions (interfaces) not concrete classes |

---

## Design Pattern Mapping

The table below maps the required 4 patterns, including creational, structural, behavioral, and one framework-enforced pattern.

| Pattern | Category | Where Implemented | Evidence |
|---|---|---|---|
| Factory Method | Creational | `OrderItem.fromCartItem(...)` creates `OrderItem` from `CartItem` | [src/main/java/com/quickcommerce/model/OrderItem.java](src/main/java/com/quickcommerce/model/OrderItem.java) |
| Adapter (Mapper-style) | Structural | `ProductMapper` adapts between domain model and persistence entity | [src/main/java/com/quickcommerce/service/ProductMapper.java](src/main/java/com/quickcommerce/service/ProductMapper.java) |
| Observer / Event Listener | Behavioral | Swing event callbacks via `MouseAdapter` in custom UI components | [src/main/java/com/quickcommerce/gui/components/StyledButton.java](src/main/java/com/quickcommerce/gui/components/StyledButton.java) |
| Repository (Spring Data JPA) | Framework-enforced | Spring repositories extend `JpaRepository` and are injected into services | [src/main/java/com/quickcommerce/persistence/repo/ProductRepository.java](src/main/java/com/quickcommerce/persistence/repo/ProductRepository.java), [src/main/java/com/quickcommerce/SpringApp.java](src/main/java/com/quickcommerce/SpringApp.java) |

---

## UML Relationships Implemented

| Relationship | Classes | Multiplicity |
|---|---|---|
| Inheritance | User ← Customer / Vendor / DeliveryPartner / Administrator | — |
| Composition | Cart → CartItem | 1 to 1..* |
| Composition | Customer → Cart | 1 to 1 |
| Composition | Order → OrderItem | 1 to 1..* |
| Composition | Order → Payment | 1 to 1 |
| Association | Customer → Order | 1 to * |
| Association | DeliveryPartner → Order | 1 to 0..* |
| Association | Vendor → Product | 1 to * |
| Association | OrderItem → Product | * to 1 |
| Association | CartItem → Product | * to 1 |

---

## How to Build & Run

This project is a Spring Boot application that launches a Swing UI. The build uses Maven and targets Java 17.

### Requirements
- Java 17 (JDK 17)
- Maven 3.6+

> The app is launched by Spring Boot (`com.quickcommerce.SpringApp`). Spring is used to wire services; the GUI runs on the AWT Event Dispatch Thread (EDT). The Spring Boot plugin repackages the app as an executable JAR.

### Build (PowerShell)
```powershell
# From the project root (where pom.xml is)
mvn -DskipTests clean package
```

### Run (PowerShell)
```powershell
# Run via Spring Boot (runs on the EDT and opens the Swing UI)
mvn spring-boot:run

# Or run the packaged jar after building
java -jar target\QuickCommerceApp-1.0.0.jar
```

### Run from an IDE
1. Open the project in **IntelliJ IDEA** or **Eclipse**
2. Ensure the project SDK is set to **Java 17** and Maven import completes
3. Run the Spring Boot application `com.quickcommerce.SpringApp` (Run as Java Application / Spring Boot)

### Troubleshooting
- If the UI does not appear, ensure the JVM is not running in headless mode. The app sets headless=false in `SpringApp`.
- If Maven fails to download Spring artifacts, check your network/proxy settings or run `mvn -U clean package`.

---

## Demo Flow (Main.java)

When you run the application, it simulates a complete grocery delivery workflow:

1. A **Vendor** registers and lists 5 products (Milk, Bread, Eggs, Rice, Chocolate)
2. A **Customer** logs in and adds items to their cart
3. The customer places an **Order** and pays via **UPI**
4. The **Vendor** confirms the order
5. A **Delivery Partner** is auto-assigned by the `OrderService`
6. The delivery partner marks the order as **Delivered**
7. The customer **tracks** the order
8. A **second order** is placed and then **cancelled** (demonstrating stock restoration)
9. The **Admin** generates a full report and monitors active orders

---

## Extending the Project

- **Add a database layer**: Replace the in-memory `List<>` stores in each service with JPA repositories
- **Add a REST API**: Wrap each service with a Spring Boot `@RestController`
- **Add Spring Security**: Integrate with the existing `login()` / `logout()` logic
- **Add a Notification system**: Implement an Observer pattern to email/SMS customers on status changes

---

## GRASP & SOLID Mapping

The table below maps main source files to applicable GRASP and SOLID principles. Click a file path to open it in your editor.

| File | GRASP Principles | SOLID Principles | Notes |
|---|---|---|---|
| [src/main/java/com/quickcommerce/controller/ProductController.java](src/main/java/com/quickcommerce/controller/ProductController.java) | Controller, Information Expert, Low Coupling | SRP, DIP | Thin controller; delegates to `IProductService`. |
| [src/main/java/com/quickcommerce/controller/UserController.java](src/main/java/com/quickcommerce/controller/UserController.java) | Controller, Information Expert | SRP, DIP | Delegates auth/registration to `IUserService`. |
| [src/main/java/com/quickcommerce/controller/OrderController.java](src/main/java/com/quickcommerce/controller/OrderController.java) | Controller | SRP, DIP | Routes UI actions to `IOrderService`. |
| [src/main/java/com/quickcommerce/service/ProductService.java](src/main/java/com/quickcommerce/service/ProductService.java) | Information Expert, Creator, High Cohesion | SRP (improved), OCP, DIP | Owns product catalogue; mapping/presentation moved out to mappers/presenters. |
| [src/main/java/com/quickcommerce/service/ProductMapper.java](src/main/java/com/quickcommerce/service/ProductMapper.java) | Pure Fabrication | SRP | Mapping logic extracted from `ProductService`. |
| [src/main/java/com/quickcommerce/service/ProductPresenter.java](src/main/java/com/quickcommerce/service/ProductPresenter.java) | Pure Fabrication | SRP | Presentation separated from business logic. |
| [src/main/java/com/quickcommerce/service/UserService.java](src/main/java/com/quickcommerce/service/UserService.java) | Information Expert, Creator (user objects) | SRP (partial), DIP | Handles registration/auth; consider extracting mapping to `UserMapper`. |
| [src/main/java/com/quickcommerce/service/OrderService.java](src/main/java/com/quickcommerce/service/OrderService.java) | Information Expert, Creator (orders) | SRP, DIP | Manages order lifecycle and delivery assignment. |
| [src/main/java/com/quickcommerce/gui/AppContext.java](src/main/java/com/quickcommerce/gui/AppContext.java) | Indirection, Low Coupling | DIP | Mediates between GUI and services; allows DI wiring. |
| [src/main/java/com/quickcommerce/SpringApp.java](src/main/java/com/quickcommerce/SpringApp.java) | Protected Variations, Indirection | DIP | Exposes service beans via interfaces to protect callers from implementation changes. |
| [src/main/java/com/quickcommerce/Main.java](src/main/java/com/quickcommerce/Main.java) | Creator, Controller (app runner) | SRP | Demo runner that assembles and exercises components. |
| [src/main/java/com/quickcommerce/service/IProductService.java](src/main/java/com/quickcommerce/service/IProductService.java) | Protected Variations | ISP, DIP | Service abstraction to decouple controllers from implementation. |
| [src/main/java/com/quickcommerce/service/IUserService.java](src/main/java/com/quickcommerce/service/IUserService.java) | Protected Variations | ISP, DIP | Abstraction for user operations. |
| [src/main/java/com/quickcommerce/service/IOrderService.java](src/main/java/com/quickcommerce/service/IOrderService.java) | Protected Variations | ISP, DIP | Abstraction for order operations. |

If you'd like, I can:
- extract `UserMapper` and `OrderMapper` (completing Creator/Information Expert separation),
- wrap Spring JPA repositories behind repository ports to strengthen Protected Variations, or
- run a full Maven build and fix any compilation issues introduced by the refactor.

Tell me which next step you want and I'll proceed.
