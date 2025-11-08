# Testing Guide

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "InventoryServiceTest"
./gradlew test --tests "InventoryControllerTest"
```

### Run Specific Test Method
```bash
./gradlew test --tests "InventoryServiceTest.testReserveInventory_Success"
```

## Test Coverage

### InventoryServiceTest

Comprehensive tests for the reserve inventory flow:

1. **testReserveInventory_Success** ✅
   - Tests successful reservation
   - Verifies inventory quantities are updated correctly
   - Checks reservation is created with correct status

2. **testReserveInventory_InsufficientInventory** ✅
   - Tests rejection when requesting more than available
   - Verifies no changes to inventory state

3. **testReserveInventory_InventoryNotFound** ✅
   - Tests error handling for invalid SKU

4. **testReserveInventory_ExactAvailableQuantity** ✅
   - Tests edge case of reserving exactly all available items

5. **testReserveInventory_MultipleReservations** ✅
   - Tests multiple sequential reservations
   - Verifies cumulative quantity updates

6. **testReserveInventory_DefaultTimeout** ✅
   - Tests default 15-minute timeout is applied

7. **testReserveInventory_CustomTimeout** ✅
   - Tests custom timeout configuration

8. **testReserveInventory_ConcurrentReservations_PreventsOverselling** ✅
   - **CRITICAL CONCURRENCY TEST**
   - 10 threads trying to reserve 15 units each from 100 available
   - Verifies pessimistic locking prevents overselling
   - Ensures total inventory integrity (available + reserved = 100)

### InventoryControllerTest

Basic controller endpoint tests:

1. **testStatusEndpoint** ✅
   - Health check endpoint

2. **testGetInventoryEndpoint** ✅
   - Get inventory details

3. **testGetNonExistentInventory** ✅
   - 404 handling

## Key Test Findings

### What Was Fixed in Your Implementation

Your original implementation had several issues that were corrected:

1. **Missing Variable Assignment**
   ```java
   // ❌ Before
   inventoryRepository.findBySku(reservationRequest.getSku())
           .orElseThrow(() -> new RuntimeException("Inventory not found"));
   if (inventoryItem.getAvailableQuantity() < reservationRequest.getQuantity()) {
   
   // ✅ After
   InventoryItem inventoryItem = inventoryRepository
           .findBySkuWithLock(reservationRequest.getSku())
           .orElseThrow(() -> new InventoryNotFoundException(...));
   ```

2. **Missing Pessimistic Locking**
   ```java
   // ❌ Before - No locking, race conditions possible
   inventoryRepository.findBySku(sku)
   
   // ✅ After - Pessimistic write lock
   inventoryRepository.findBySkuWithLock(sku)
   ```

3. **Missing Inventory Quantity Updates**
   ```java
   // ❌ Before - Only created reservation, didn't update inventory
   Reservation reservation = new Reservation();
   reservationRepository.save(reservation);
   
   // ✅ After - Updates both inventory and creates reservation
   inventoryItem.setAvailableQuantity(available - quantity);
   inventoryItem.setReservedQuantity(reserved + quantity);
   inventoryRepository.save(inventoryItem);
   // ... then create reservation
   ```

4. **Missing Imports**
   - Added: `Reservation`, `LocalDateTime`, `UUID`
   - Added: `InsufficientInventoryException`, `InventoryNotFoundException`

5. **Fixed Typo**
   ```java
   // ❌ Before
   reservationrRepository  // Double 'r'
   
   // ✅ After
   reservationRepository
   ```

6. **Transaction Isolation**
   ```java
   // ✅ Added
   @Transactional(isolation = Isolation.READ_COMMITTED)
   ```

## What's Still TODO

The following methods need implementation:

1. **`releaseReservation(String reservationId)`**
   - Find reservation by ID
   - Update inventory (restore available, decrease reserved)
   - Update reservation status to RELEASED

2. **`confirmReservation(String reservationId, String orderId)`**
   - Find reservation by ID
   - Update totalQuantity (consume the items)
   - Update reservation status to CONFIRMED
   - Link to order ID

3. **`cleanupExpiredReservations()` (Scheduled)**
   - Find expired ACTIVE reservations
   - For each, restore inventory
   - Update status to EXPIRED

## Test Isolation

Tests use `@DirtiesContext` to ensure each test starts with a fresh database context. This prevents test interference but makes tests slower.

For faster development, you can:
- Run individual test classes
- Use `@Transactional` with `@Rollback` on specific tests
- Mock dependencies for unit tests

## Concurrency Testing

The concurrency test is critical for validating the locking mechanism:

```java
testReserveInventory_ConcurrentReservations_PreventsOverselling()
```

This test:
- Spawns 10 threads
- Each tries to reserve 15 units
- Total requested: 150 units
- Available: 100 units
- **Expected**: Only 6-7 should succeed, rest should fail
- **Validates**: No overselling occurs

Run this test multiple times to verify consistency:
```bash
for i in {1..5}; do
  ./gradlew test --tests "InventoryServiceTest.testReserveInventory_ConcurrentReservations_PreventsOverselling"
done
```

## Performance Testing

For load testing, consider:
- JMeter or Gatling for HTTP load tests
- Custom Java threads for service layer tests
- Database connection pool sizing
- Transaction timeout configuration

## Debugging Tests

View detailed HTML test reports:
```bash
open build/reports/tests/test/index.html
# Or on Linux:
xdg-open build/reports/tests/test/index.html
```

Enable debug logging in tests:
```properties
# src/test/resources/application.properties
logging.level.com.interview.practice.inventory=DEBUG
logging.level.org.springframework.transaction=TRACE
logging.level.org.hibernate.SQL=DEBUG
```

