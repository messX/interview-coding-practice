# 📦 Inventory Reservation System

## Problem Statement
Build an inventory management system that handles reservations, releases, and prevents overselling in a concurrent environment.

**Difficulty**: Hard | **Category**: Concurrency  
**Companies**: Amazon, Uber, Stripe  
**Time**: 45-60 minutes

## Requirements

### Core Features to Implement
- ✅ Reserve inventory items for orders
- ✅ Release reservations (timeout or explicit)
- ✅ Handle concurrent reservation attempts
- ✅ Prevent overselling (race conditions)
- ✅ Query available inventory
- ✅ Support reservation expiration

### Key Concepts to Demonstrate
- Pessimistic vs optimistic locking
- Race condition handling
- Transaction management (ACID)
- Time-based expiration (TTL)
- Database isolation levels

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (in-memory for development)
- **Gradle** (build tool)

## Project Structure

```
inventory-reservation-system/
├── src/main/java/com/interview/practice/inventory/
│   ├── InventoryReservationApplication.java  # Main application
│   ├── model/
│   │   ├── InventoryItem.java              # Inventory entity with optimistic lock
│   │   └── Reservation.java                # Reservation entity
│   ├── repository/
│   │   ├── InventoryItemRepository.java    # JPA repo with locking queries
│   │   └── ReservationRepository.java      # Reservation repo
│   ├── service/
│   │   └── InventoryService.java           # TODO: Implement business logic
│   ├── controller/
│   │   └── InventoryController.java        # REST endpoints (minimal)
│   └── exception/
│       ├── InsufficientInventoryException.java
│       └── ReservationNotFoundException.java
├── src/main/resources/
│   ├── application.properties              # Configuration
│   ├── schema.sql                         # Database schema
│   └── data.sql                           # Sample data
└── src/test/java/
    └── InventoryControllerTest.java       # Basic tests
```

## Quick Start

### Option 1: Use Setup Script (Recommended)
```bash
./setup.sh
```

This will:
- Check Java version
- Build the project
- Run tests
- Show you how to start the app

### Option 2: Manual Setup

#### 1. Build the project
```bash
./gradlew build
```

#### 2. Run the application
```bash
./gradlew bootRun
# Or use the convenience script:
./run.sh
```

##### 3. Test the endpoints
```bash
# Status check
curl http://localhost:8080/api/inventory/status

# Get inventory for a SKU
curl http://localhost:8080/api/inventory/LAPTOP-001

# Reserve inventory
curl -X POST http://localhost:8080/api/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{"sku":"LAPTOP-001","quantity":10,"timeoutMinutes":15}'

# Release reservation (use reservation ID from previous response)
curl -X POST http://localhost:8080/api/inventory/release/{reservationId}

# Confirm reservation
curl -X POST "http://localhost:8080/api/inventory/confirm/{reservationId}?orderId=ORDER-123"
```

### Option 3: Use Postman Collection
Import the Postman collection from `postman/Inventory-Reservation-System.postman_collection.json`

The collection includes:
- Health check
- Get inventory
- Reserve inventory (auto-saves reservation ID)
- Release reservation
- Confirm reservation
- H2 Console link

### 4. Access H2 Console (for debugging)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (leave empty)

### 5. Run tests
```bash
./gradlew test
```

## Implementation Tasks

### Service Layer (InventoryService.java)

Implement these methods in `InventoryService`:

#### 1. `reserveInventory(String sku, Integer quantity, Integer timeoutMinutes)`
- Use pessimistic locking: `findBySkuWithLock(sku)`
- Check if `availableQuantity >= quantity`
- Update `availableQuantity` and `reservedQuantity`
- Create and save `Reservation` entity
- Set expiration time
- Return reservation ID

**Key considerations:**
- Transaction isolation level
- Race condition prevention
- Idempotency

#### 2. `releaseReservation(String reservationId)`
- Find reservation by ID
- Verify status is ACTIVE
- Get inventory item with lock
- Restore `availableQuantity` and `reservedQuantity`
- Update reservation status to RELEASED

#### 3. `confirmReservation(String reservationId, String orderId)`
- Find and validate reservation
- Update `totalQuantity` (consume the items)
- Mark reservation as CONFIRMED
- Link to order ID

#### 4. `cleanupExpiredReservations()` (Scheduled Job)
- Find expired ACTIVE reservations
- For each, restore inventory
- Update status to EXPIRED
- Handle failures gracefully

## Sample Data

The database is pre-seeded with:
- `LAPTOP-001` - Gaming Laptop Pro (100 units)
- `PHONE-001` - Smartphone X (500 units)
- `TABLET-001` - Tablet Ultra (50 units)
- `MONITOR-001` - 4K Monitor (200 units)
- `KEYBOARD-001` - Mechanical Keyboard (150 units)

## Interview Focus Areas

### 1. Concurrency Control
- **Pessimistic Locking**: Use `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- **Optimistic Locking**: Use `@Version` field
- **Trade-offs**: Discuss when to use each approach

### 2. Transaction Management
- Isolation levels: `READ_COMMITTED`, `REPEATABLE_READ`
- Transaction boundaries
- Deadlock prevention

### 3. Race Conditions
- Multiple concurrent reservations
- Double-booking prevention
- Lost update problem

### 4. System Design Considerations
- Reservation timeout handling
- Cleanup job frequency
- Database sharding strategy
- Event-driven architecture (optional)

## Testing Scenarios

Test these edge cases:
1. **Concurrent reservations** - Same SKU, multiple threads
2. **Overselling prevention** - Reserve more than available
3. **Expired reservations** - Verify cleanup job
4. **Double release** - Release same reservation twice
5. **Race conditions** - High concurrency load testing

## Next Steps

1. Implement the TODO methods in `InventoryService`
2. Add REST endpoints for reserve/release/confirm
3. Write comprehensive tests
4. Add load testing for concurrency
5. Document your design decisions

## Resources

- [Spring Data JPA Locking](https://www.baeldung.com/jpa-pessimistic-locking)
- [Transaction Isolation Levels](https://www.postgresql.org/docs/current/transaction-iso.html)
- [Preventing Overselling in E-commerce](https://engineering.zalando.com/posts/2021/03/inventory-management.html)

