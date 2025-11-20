//package br.edu.fatecpg.BenucciArtesanato.controller;
//
//import br.edu.fatecpg.BenucciArtesanato.model.Category;
//import br.edu.fatecpg.BenucciArtesanato.model.Product;
//import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
//import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/seed")
//public class SeedController {
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @PostMapping("/products")
//    public ResponseEntity<String> seedProducts() {
//        try {
//            // Criar categorias
//            Category mandala = createOrGetCategory("Mandala");
//            Category chaveiro = createOrGetCategory("Chaveiro");
//            Category portaChaves = createOrGetCategory("Porta chaves");
//            Category gato = createOrGetCategory("Gato");
//            Category ima = createOrGetCategory("Imã");
//
//            // Lista para armazenar produtos
//            List<Product> products = new ArrayList<>();
//
//            // Produto 1
//            Product p1 = new Product();
//            p1.setName("Mandala Colorida");
//            p1.setDescription("Linda mandala colorida feita à mão com detalhes únicos");
//            p1.setPrice(new BigDecimal("35.90"));
//            p1.setStock(10);
//            p1.setImageUrl("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=400&fit=crop");
//            p1.setCategory(mandala);
//            products.add(p1);
//
//            // Produto 2
//            Product p2 = new Product();
//            p2.setName("Chaveiro Gato");
//            p2.setDescription("Chaveiro em formato de gato, perfeito para presentear");
//            p2.setPrice(new BigDecimal("15.50"));
//            p2.setStock(25);
//            p2.setImageUrl("https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400&h=400&fit=crop");
//            p2.setCategory(chaveiro);
//            products.add(p2);
//
//            // Produto 3
//            Product p3 = new Product();
//            p3.setName("Porta Chaves Decorado");
//            p3.setDescription("Porta chaves decorativo para organizar suas chaves com estilo");
//            p3.setPrice(new BigDecimal("45.00"));
//            p3.setStock(8);
//            p3.setImageUrl("https://images.unsplash.com/photo-1582139329536-e7284fece509?w=400&h=400&fit=crop");
//            p3.setCategory(portaChaves);
//            products.add(p3);
//
//            // Produto 4
//            Product p4 = new Product();
//            p4.setName("Imã Geladeira");
//            p4.setDescription("Imã decorativo para geladeira, diversos modelos disponíveis");
//            p4.setPrice(new BigDecimal("12.00"));
//            p4.setStock(30);
//            p4.setImageUrl("https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=400&h=400&fit=crop");
//            p4.setCategory(ima);
//            products.add(p4);
//
//            // Produto 5
//            Product p5 = new Product();
//            p5.setName("Gato Decorativo");
//            p5.setDescription("Gato decorativo de parede, ideal para decoração");
//            p5.setPrice(new BigDecimal("28.90"));
//            p5.setStock(12);
//            p5.setImageUrl("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=400&fit=crop");
//            p5.setCategory(gato);
//            products.add(p5);
//
//            // Produto 6
//            Product p6 = new Product();
//            p6.setName("Mandala Grande");
//            p6.setDescription("Mandala grande e detalhada, peça única de artesanato");
//            p6.setPrice(new BigDecimal("65.00"));
//            p6.setStock(5);
//            p6.setImageUrl("https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=400&h=400&fit=crop");
//            p6.setCategory(mandala);
//            products.add(p6);
//
//            // Produto 7
//            Product p7 = new Product();
//            p7.setName("Chaveiro Personalizado");
//            p7.setDescription("Chaveiro personalizado com nome ou iniciais");
//            p7.setPrice(new BigDecimal("18.90"));
//            p7.setStock(20);
//            p7.setImageUrl("https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=400&h=400&fit=crop");
//            p7.setCategory(chaveiro);
//            products.add(p7);
//
//            // Produto 8
//            Product p8 = new Product();
//            p8.setName("Porta Chaves Rústico");
//            p8.setDescription("Porta chaves em estilo rústico, madeira de qualidade");
//            p8.setPrice(new BigDecimal("52.00"));
//            p8.setStock(6);
//            p8.setImageUrl("https://images.unsplash.com/photo-1565375321441-b118982f3e86?w=400&h=400&fit=crop");
//            p8.setCategory(portaChaves);
//            products.add(p8);
//
//            // Produto 9
//            Product p9 = new Product();
//            p9.setName("Kit Imãs Sortidos");
//            p9.setDescription("Kit com 5 imãs decorativos sortidos");
//            p9.setPrice(new BigDecimal("35.00"));
//            p9.setStock(15);
//            p9.setImageUrl("https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=400&h=400&fit=crop");
//            p9.setCategory(ima);
//            products.add(p9);
//
//            // Produto 10
//            Product p10 = new Product();
//            p10.setName("Gato Amigurumi");
//            p10.setDescription("Gato de crochê (amigurumi) feito à mão");
//            p10.setPrice(new BigDecimal("42.00"));
//            p10.setStock(8);
//            p10.setImageUrl("https://images.unsplash.com/photo-1529778873920-4da4926a72c2?w=400&h=400&fit=crop");
//            p10.setCategory(gato);
//            products.add(p10);
//
//            // Salvar todos os produtos
//            productRepository.saveAll(products);
//
//            return ResponseEntity.ok(
//                    String.format("✅ Banco populado com sucesso! %d produtos criados.", products.size())
//            );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                    .body("❌ Erro ao popular banco: " + e.getMessage());
//        }
//    }
//
//    private Category createOrGetCategory(String name) {
//        return categoryRepository.findByName(name)
//                .orElseGet(() -> {
//                    Category category = new Category();
//                    category.setName(name);
//                    return categoryRepository.save(category);
//                });
//    }
//}