-- ========================================
-- Banco de Dados para E-commerce
-- ========================================

-- Usuário
CREATE TABLE Usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20),
    endereco TEXT,
    tipo VARCHAR(10) CHECK (tipo IN ('cliente', 'admin')) NOT NULL
);

-- Categoria
CREATE TABLE Categoria (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT
);

-- Produto
CREATE TABLE Produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10,2) NOT NULL,
    estoque INT NOT NULL DEFAULT 0,
    categoria_id INT REFERENCES Categoria(id) ON DELETE SET NULL,
    imagem_url TEXT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pedido
CREATE TABLE Pedido (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES Usuario(id) ON DELETE CASCADE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('em preparação', 'enviado', 'entregue')) DEFAULT 'em preparação',
    tipo_entrega VARCHAR(10) CHECK (tipo_entrega IN ('retirada', 'entrega')),
    endereco_entrega TEXT,
    valor_total NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- ItemPedido (tabela intermediária)
CREATE TABLE ItemPedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT REFERENCES Pedido(id) ON DELETE CASCADE,
    produto_id INT REFERENCES Produto(id) ON DELETE CASCADE,
    quantidade INT NOT NULL,
    preco_unitario NUMERIC(10,2) NOT NULL
);

-- Avaliação
CREATE TABLE Avaliacao (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES Usuario(id) ON DELETE CASCADE,
    produto_id INT REFERENCES Produto(id) ON DELETE CASCADE,
    nota INT CHECK (nota >= 1 AND nota <= 5),
    comentario TEXT,
    data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pagamento
CREATE TABLE Pagamento (
    id SERIAL PRIMARY KEY,
    pedido_id INT REFERENCES Pedido(id) ON DELETE CASCADE,
    tipo VARCHAR(20) CHECK (tipo IN ('Mercado Pago', 'Pix', 'cartão')),
    status VARCHAR(10) CHECK (status IN ('pendente', 'aprovado', 'recusado')) DEFAULT 'pendente',
    data_pagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valor NUMERIC(10,2) NOT NULL
);

-- Recomendação de Produto
CREATE TABLE RecomendacaoProduto (
    id SERIAL PRIMARY KEY,
    produto_origem_id INT REFERENCES Produto(id) ON DELETE CASCADE,
    produto_recomendado_id INT REFERENCES Produto(id) ON DELETE CASCADE
);

