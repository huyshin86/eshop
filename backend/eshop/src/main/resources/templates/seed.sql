INSERT INTO users (email, password_hash, role, first_name, last_name, phone_number, address) VALUES
('admin@localhost', '$2b$12$XsjLcbuu5NRARZC2n/KG9.KjDmJD/EgAkzzLB7Y/uGSPNpA24L8NK', 'ADMIN', 'admin', 'admin', '123456789', null),
('customer@localhost', '$2b$12$XsjLcbuu5NRARZC2n/KG9.KjDmJD/EgAkzzLB7Y/uGSPNpA24L8NK', 'CUSTOMER', 'customer', 'customer', '123456789', 'abc street');

INSERT INTO categories (category_name, slug, description, image_url) VALUES
('Test', 'test', null, 'test-local-png');

INSERT INTO products (category_id, product_name, description, price, image_url, stock) VALUES
(1, 'Test product', 'This is test product', 69.69, 'test-product-local-png', 69);

INSERT INTO orders (user_id, order_number, order_status, subtotal, grand_total, shipping_address) VALUES
(2, 'abc123', 'CANCELLED', 69.69, 69.69, 'abc street');

INSERT INTO order_items (order_id, product_id, quantity, unit_price, total) VALUES
(1, 1, 1, 69.69, 69.69);