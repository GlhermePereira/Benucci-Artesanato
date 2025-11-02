package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupabaseService supabaseService;
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllDTO() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        return convertToDTO(product);
    }
    @Transactional
    public Product createProduct(ProductDTO dto, MultipartFile file) throws IOException {
        // Buscar categoria
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));

        // Redimensionar imagem
        BufferedImage original = ImageIO.read(file.getInputStream());
        int newWidth = 800;
        int newHeight = 600;
        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        // Converter imagem para bytes
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", os);
        byte[] imageBytes = os.toByteArray();

        // Enviar para Supabase e pegar a URL
        String imageUrl = supabaseService.uploadImage(file.getOriginalFilename(), imageBytes);

        // Criar produto
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(imageUrl); // só salvar a URL no banco
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Atualizar campos
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());

        // Atualizar categoria se fornecida
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método auxiliar para converter Product -> ProductDTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());

        // Preencher categoryId e objeto category
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());

            ProductDTO.CategoryDTO categoryDTO = new ProductDTO.CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }

        return dto;
    }
}