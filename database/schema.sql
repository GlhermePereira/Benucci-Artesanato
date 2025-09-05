-- ========================================
-- Drop all tables (in order to avoid FK issues)
-- ========================================

DROP TABLE IF EXISTS product_recommendation CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS order_item CASCADE;
DROP TABLE IF EXISTS "order" CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;

-- ========================================
-- Database for E-commerce
-- ========================================

-- User
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    type VARCHAR(10) CHECK (type IN ('customer', 'admin')) NOT NULL
);

-- Category
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Product
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category_id INT REFERENCES category(id) ON DELETE SET NULL,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order
CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('preparing', 'shipped', 'delivered')) DEFAULT 'preparing',
    delivery_type VARCHAR(10) CHECK (delivery_type IN ('pickup', 'delivery')),
    delivery_address TEXT,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- OrderItem (intermediate table)
CREATE TABLE order_item (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(id) ON DELETE CASCADE,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    quantity INT NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL
);

-- Review
CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment
CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(id) ON DELETE CASCADE,
    type VARCHAR(20) CHECK (type IN ('Mercado Pago', 'Pix', 'card')),
    status VARCHAR(10) CHECK (status IN ('pending', 'approved', 'declined')) DEFAULT 'pending',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(10,2) NOT NULL
);

-- Product Recommendation
CREATE TABLE product_recommendation (
    id SERIAL PRIMARY KEY,
    source_product_id INT REFERENCES product(id) ON DELETE CASCADE,
    recommended_product_id INT REFERENCES product(id) ON DELETE CASCADE
);
