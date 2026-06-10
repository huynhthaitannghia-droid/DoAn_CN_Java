-- =============================================
-- XÓA VÀ TẠO LẠI DATABASE
-- =============================================
DROP DATABASE IF EXISTS cafe_db;
CREATE DATABASE cafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cafe_db;

-- =============================================
-- TẠO DATABASE
-- =============================================
CREATE DATABASE IF NOT EXISTS cafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cafe_db;

-- NHỚ CHẠY PROJECT TRƯỚC

-- =============================================
-- 1. CATEGORIES
-- =============================================
INSERT INTO categories (name, description, image_path, active, slug, sort_order) VALUES
('Cà phê', 'Các loại cà phê đặc sắc', NULL, 1, 'ca-phe', 1),
('Trà', 'Trà sữa và trà trái cây', NULL, 1, 'tra', 2),
('Bánh ngọt', 'Bánh và dessert', NULL, 1, 'banh-ngot', 3);

-- =============================================
-- 2. USERS (password = BCrypt của "123456")
-- =============================================
INSERT INTO users (email, password, full_name, phone, role, enabled, locked) VALUES
('admin@cafe.com',
 '$2a$10$1eVdENtwfap91U3o28c8LOU4q32W79AB18DoT6Tr7ZAXnDzMCqPPC',
 'Admin Cafe', '0901234567', 'ADMIN', 1, 0),
('user1@gmail.com',
 '$2a$10$1eVdENtwfap91U3o28c8LOU4q32W79AB18DoT6Tr7ZAXnDzMCqPPC',
 'Nguyễn Văn An', '0912345678', 'USER', 1, 0),
('user2@gmail.com',
 '$2a$10$1eVdENtwfap91U3o28c8LOU4q32W79AB18DoT6Tr7ZAXnDzMCqPPC',
 'Trần Thị Bình', '0923456789', 'USER', 1, 0);

-- =============================================
-- 3. PRODUCTS
-- =============================================
INSERT INTO products (name, description, price, sale_price, stock, active, is_featured, category_id) VALUES
('Cà phê đen', 'Cà phê phin truyền thống đậm đà', 25000, NULL, 100, 1, 1, 1),
('Cà phê sữa', 'Cà phê phin pha sữa đặc', 30000, 28000, 100, 1, 1, 1),
('Bạc xỉu', 'Cà phê sữa ít cà phê nhiều sữa', 32000, NULL, 80, 1, 0, 1),
('Trà sữa trân châu', 'Trà sữa Đài Loan trân châu đen', 45000, 40000, 50, 1, 1, 2),
('Trà đào cam sả', 'Trà đào thơm mát kết hợp cam và sả', 40000, NULL, 60, 1, 0, 2),
('Bánh tiramisu', 'Bánh tiramisu Ý thơm ngon', 55000, 50000, 30, 1, 1, 3);

-- =============================================
-- 4. PRODUCT IMAGES
-- =============================================
INSERT INTO product_images (image_path, is_primary, product_id) VALUES
('/images/default.png', 1, 1),
('/images/default.png', 1, 2),
('/images/default.png', 1, 3),
('/images/default.png', 1, 4),
('/images/default.png', 1, 5),
('/images/default.png', 1, 6);

-- =============================================
-- 5. COUPONS
-- =============================================
INSERT INTO coupons (code, discount_type, discount_value, min_order_value, max_uses, used_count, expiry_date, active) VALUES
('WELCOME10', 'PERCENT', 10, 50000, 100, 0, '2027-12-31 23:59:59', 1),
('GIAM20K',   'FIXED',   20000, 100000, 50, 0, '2027-12-31 23:59:59', 1);

-- =============================================
-- 6. ORDERS + ORDER ITEMS
-- =============================================
INSERT INTO orders (user_id, status, total_price, final_price, coupon_id, discount_amount, shipping_address, phone, payment_method, note) VALUES
(2, 'COMPLETED', 75000, 75000, NULL, 0, '123 Nguyễn Huệ, Q1, TP.HCM', '0912345678', 'COD', NULL),
(3, 'PENDING',   85000, 76500, 1,    8500, '456 Lê Lợi, Q1, TP.HCM',    '0923456789', 'COD', 'Giao buổi sáng');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 25000),
(1, 2, 1, 28000),
(1, 4, 1, 40000),  -- sale price
(2, 4, 1, 40000),
(2, 6, 1, 50000);  -- sale price

-- =============================================
-- 7. REVIEWS
-- =============================================
INSERT INTO reviews (user_id, product_id, rating, comment, admin_reply) VALUES
(2, 1, 5, 'Cà phê ngon, đậm đà!', 'Cảm ơn bạn đã ủng hộ!'),
(2, 4, 4, 'Trà sữa ngon nhưng hơi ngọt.', NULL),
(3, 4, 5, 'Tuyệt vời, sẽ order lại!', NULL);