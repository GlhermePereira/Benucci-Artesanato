-- ========================================
-- Drop all tables (Ordem Inversa para FKs)
-- ========================================
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS order_item CASCADE;
DROP TABLE IF EXISTS "order" CASCADE;
DROP TABLE IF EXISTS product_theme CASCADE; -- Novo: Tabela N:N Produto-Tema
DROP TABLE IF EXISTS subcategory_theme CASCADE;
DROP TABLE IF EXISTS theme CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS subcategory CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS product_image CASCADE;

-- ========================================
-- User
-- ========================================
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE,
    role VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- CATEGORY
-- ========================================
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(160) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ========================================
-- SUBCATEGORY (category_id NOT NULL reforça 1:N obrigatório)
-- Herda de CATEGORY
-- ========================================
CREATE TABLE subcategory (
    id SERIAL PRIMARY KEY,
    category_id INT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(160) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (category_id, slug)
);

-- ========================================
-- THEME
-- ========================================
CREATE TABLE theme (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(160) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ========================================
-- SUBCATEGORY_THEME (N:M Resolução - PK COMPOSTA)
-- Define os Temas VÁLIDOS/PERMITIDOS para uma Subcategoria.
-- ESSA TABELA FOI MANTIDA PARA A REGRA DE NEGÓCIO DE VALIDAÇÃO.
-- ========================================
CREATE TABLE subcategory_theme (
    subcategory_id INT NOT NULL REFERENCES subcategory(id) ON DELETE CASCADE,
    theme_id INT NOT NULL REFERENCES theme(id) ON DELETE CASCADE,
    PRIMARY KEY (subcategory_id, theme_id)
);

-- ========================================
-- Product (Herda a Subcategoria)
-- ========================================
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    subcategory_id INT NOT NULL REFERENCES subcategory(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- NOVO: PRODUCT_THEME (N:M Resolução - PK COMPOSTA)
-- Permite que um Produto tenha múltiplos Temas específicos.
-- ========================================
CREATE TABLE product_theme (
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    theme_id INT NOT NULL REFERENCES theme(id) ON DELETE CASCADE,
    -- PK Composta que resolve o relacionamento N:M
    PRIMARY KEY (product_id, theme_id)
);

CREATE TABLE product_image (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL
);


-- ========================================
-- Order (user_id NOT NULL para garantir (1, 1))
-- ========================================
CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('pending', 'preparing', 'shipped', 'delivered', 'canceled')) DEFAULT 'pending',
    delivery_type VARCHAR(10) CHECK (delivery_type IN ('pickup', 'delivery')),
    delivery_address TEXT,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    mp_preference_id VARCHAR(100)
);

-- ========================================
-- OrderItem (N:M Resolução - PK COMPOSTA)
-- ========================================
CREATE TABLE order_item (
    order_id INT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,

    product_name VARCHAR(100) NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),

    PRIMARY KEY (order_id, product_id)
);

-- ========================================
-- Review (N:M Resolução - PK COMPOSTA)
-- ========================================
CREATE TABLE review (
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES product(id) ON DELETE CASCADE,

    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, product_id)
);

-- ========================================
-- Payment (1:1 com UNIQUE Constraint)
-- ========================================
CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,

    payment_method VARCHAR(20) CHECK (payment_method IN ('Mercado Pago', 'Pix', 'Card')),
    mp_preference_id VARCHAR(100),
    status VARCHAR(10) CHECK (status IN ('pending', 'approved', 'declined')) DEFAULT 'pending',
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),

    UNIQUE (order_id)
);


-- ========================================
-- Criação de Índices
-- ========================================
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_subcategory_category ON subcategory(category_id);
CREATE INDEX idx_product_subcategory ON product(subcategory_id);
-- NOVO: Índice para a nova tabela de relacionamento
CREATE INDEX idx_product_theme_product ON product_theme(product_id);
CREATE INDEX idx_product_theme_theme ON product_theme(theme_id);
CREATE INDEX idx_order_user ON "order"(user_id);
CREATE INDEX idx_order_status ON "order"(status);
CREATE INDEX idx_order_item_order ON order_item(order_id);
CREATE INDEX idx_order_item_product ON order_item(product_id);
CREATE INDEX idx_review_product ON review(product_id);
CREATE INDEX idx_review_user ON review(user_id);
CREATE INDEX idx_payment_order ON payment(order_id);