-- ========================================
-- Inserts de exemplo para E-commerce
-- ========================================

-- Usuários
INSERT INTO Usuario (nome, email, senha, telefone, endereco, tipo) VALUES
('João Silva', 'joao@email.com', 'hash_senha1', '11999999999', 'Rua A, 123, São Paulo', 'cliente'),
('Maria Souza', 'maria@email.com', 'hash_senha2', '11988888888', 'Rua B, 456, Rio de Janeiro', 'cliente'),
('Admin', 'admin@email.com', 'hash_admin', '11977777777', 'Av. Central, 100, Brasília', 'admin');

-- Categorias
INSERT INTO Categoria (nome, descricao) VALUES
('Eletrônicos', 'Produtos eletrônicos em geral'),
('Livros', 'Livros físicos e digitais'),
('Roupas', 'Roupas masculinas e femininas');

-- Produtos
INSERT INTO Produto (nome, descricao, preco, estoque, categoria_id, imagem_url) VALUES
('Smartphone XYZ', 'Smartphone com 128GB, 6GB RAM', 1999.90, 50, 1, 'https://link-imagem.com/xyz.jpg'),
('Notebook ABC', 'Notebook i5, 16GB RAM, 512GB SSD', 3499.90, 20, 1, 'https://link-imagem.com/abc.jpg'),
('Livro "Memórias do Subsolo"', 'Clássico de Dostoiévski', 39.90, 100, 2, 'https://link-imagem.com/livro.jpg'),
('Camiseta Casual', 'Camiseta de algodão, tamanho M', 59.90, 200, 3, 'https://link-imagem.com/camiseta.jpg');

-- Pedidos
INSERT INTO Pedido (usuario_id, status, tipo_entrega, endereco_entrega, valor_total) VALUES
(1, 'em preparação', 'entrega', 'Rua A, 123, São Paulo', 2039.80),
(2, 'enviado', 'retirada', 'Loja Central', 3499.90);

-- Itens do Pedido
INSERT INTO ItemPedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES
(1, 1, 1, 1999.90),
(1, 3, 1, 39.90),
(2, 2, 1, 3499.90);

-- Avaliações
INSERT INTO Avaliacao (usuario_id, produto_id, nota, comentario) VALUES
(1, 1, 5, 'Excelente smartphone, recomendo!'),
(1, 3, 4, 'Livro interessante, mas difícil de ler'),
(2, 2, 5, 'Notebook muito rápido e eficiente');

-- Pagamentos
INSERT INTO Pagamento (pedido_id, tipo, status, valor) VALUES
(1, 'Pix', 'aprovado', 2039.80),
(2, 'Cartão', 'pendente', 3499.90);

-- Recomendações de Produto
INSERT INTO RecomendacaoProduto (produto_origem_id, produto_recomendado_id) VALUES
(1, 2),
(3, 4),
(2, 1);

