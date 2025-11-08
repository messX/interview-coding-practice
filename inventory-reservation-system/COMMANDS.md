# Quick Command Reference

## Build & Run

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

## Testing Endpoints

```bash
# Health check
curl http://localhost:8080/api/inventory/status

# Get inventory
curl http://localhost:8080/api/inventory/LAPTOP-001
curl http://localhost:8080/api/inventory/PHONE-001

# Pretty print JSON
curl -s http://localhost:8080/api/inventory/status | python3 -m json.tool
```

## Database Access

**H2 Console:** http://localhost:8080/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (leave empty)

**Sample Queries:**
```sql
-- View all inventory
SELECT * FROM inventory_items;

-- Check specific SKU
SELECT * FROM inventory_items WHERE sku = 'LAPTOP-001';

-- View reservations
SELECT * FROM reservations;

-- Check reservation status
SELECT r.reservation_id, r.quantity, r.status, r.expires_at, i.sku, i.product_name
FROM reservations r
JOIN inventory_items i ON r.inventory_item_id = i.id;
```

## Development Workflow

1. **Start the app:** `./gradlew bootRun`
2. **Make changes** to Java files
3. **Stop and restart** (Ctrl+C, then `./gradlew bootRun`)
4. **Run tests:** `./gradlew test`
5. **Check H2 console** to verify database state

## Hot Reload (Optional)

For faster development, use Spring Boot DevTools (already included):
- Make changes to Java files
- Application will auto-restart

## Troubleshooting

**Port already in use:**
```bash
# Find process on port 8080
lsof -i :8080
# Kill it
kill -9 <PID>
```

**Clean everything:**
```bash
./gradlew clean
rm -rf build .gradle
./gradlew build
```

**View logs:**
```bash
# Application logs are in console output
# Or check build/test-results/ for test reports
```

