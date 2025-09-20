-- ========================================
-- Drop all tables (avoid FK issues)
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
-- User
-- ========================================
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    cpf VARCHAR(14),
    phone_number VARCHAR(20),
    address TEXT,
    type VARCHAR(10) CHECK (type IN ('customer', 'admin')) NOT NULL
);

-- ========================================
-- Category
-- ========================================
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- ========================================
-- Product
-- ========================================
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

-- ========================================
-- Order
-- ========================================
CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('pending', 'preparing', 'shipped', 'delivered', 'canceled')) DEFAULT 'pending',
    delivery_type VARCHAR(10) CHECK (delivery_type IN ('pickup', 'delivery')),
    delivery_address TEXT,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    mp_preference_id VARCHAR(100) -- para integrar com Mercado Pago
);

-- ========================================
-- OrderItem (join table)
-- ========================================
CREATE TABLE order_item (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(id) ON DELETE CASCADE,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    product_name VARCHAR(100) NOT NULL, -- salvar histórico do nome
    unit_price NUMERIC(10,2) NOT NULL,
    quantity INT NOT NULL
);

-- ========================================
-- Review
-- ========================================
CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
    product_id INT REFERENCES product(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- Payment
-- ========================================
CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(id) ON DELETE CASCADE,
    payment_method VARCHAR(20) CHECK (payment_method IN ('Mercado Pago', 'Pix', 'Card')),
    mp_preference_id VARCHAR(100),
    status VARCHAR(10) CHECK (status IN ('pending', 'approved', 'declined')) DEFAULT 'pending',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(10,2) NOT NULL
);

-- ========================================
-- Product Recommendation
-- ========================================
CREATE TABLE product_recommendation (
    id SERIAL PRIMARY KEY,
    source_product_id INT REFERENCES product(id) ON DELETE CASCADE,
    recommended_product_id INT REFERENCES product(id) ON DELETE CASCADE
);
