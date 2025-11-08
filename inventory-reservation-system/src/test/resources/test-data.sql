-- Clean up existing data
DELETE FROM reservations;
DELETE FROM inventory_items;

-- Insert test inventory data
INSERT INTO inventory_items (sku, product_name, total_quantity, available_quantity, reserved_quantity, version, created_at, updated_at)
VALUES
    ('LAPTOP-001', 'Gaming Laptop Pro', 100, 100, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PHONE-001', 'Smartphone X', 500, 500, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('TABLET-001', 'Tablet Ultra', 50, 50, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MONITOR-001', '4K Monitor', 200, 200, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('KEYBOARD-001', 'Mechanical Keyboard', 150, 150, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

