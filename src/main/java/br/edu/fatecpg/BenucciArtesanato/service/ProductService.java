package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.*;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupabaseService supabaseService;
    @Autowired
    private SubcategoryRepository SubcategoryRepository;
    @Autowired
    private  ThemeRepository themeRepository;
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

    // Assumindo que você usa este ProductCreationDTO para a entrada (Input)
// Se não, você pode manter o nome ProductDTO e ignorar a sugestão abaixo.
    @Transactional
    public Product createProduct(ProductDTO dto, List<MultipartFile> images) {

        SubCategory subcategory = SubcategoryRepository.findById(dto.getSubcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategoria não encontrada"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSubcategory(subcategory);

        product = productRepository.save(product);

        if (images != null && !images.isEmpty()) {

            validateFiles(images);

            for (MultipartFile file : images) {

                byte[] processed = null;
                try {
                    processed = resizeImage(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String imageUrl = supabaseService.uploadImage(
                        file.getOriginalFilename(),
                        processed
                );

                ProductImage pi = new ProductImage();
                pi.setImageUrl(imageUrl);

                product.addImage(pi); // adiciona no produto
            }
        }

        return productRepository.save(product);
    }


    private void validateFiles(List<MultipartFile> files) {
        // 1. Definições de Regras
        final List<String> allowedTypes = List.of(
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/webp",
                "application/pdf"
        );
        final long maxSize = 5 * 1024 * 1024; // 5 MB

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo de imagem foi fornecido.");
        }

        // 2. Iteração e Validação de Cada Arquivo
        for (MultipartFile file : files) {

            // Verifica se o arquivo está vazio
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Um dos arquivos enviados está vazio.");
            }

            // Validação de Tipo (Content Type)
            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("Formato de arquivo não permitido: " + contentType);
            }

            // Validação de Tamanho
            if (file.getSize() > maxSize) {
                // Converte o limite de tamanho para MB para ser mais amigável na mensagem de erro
                String maxMB = String.format("%.0fMB", (double) maxSize / (1024 * 1024));
                throw new IllegalArgumentException("Arquivo '" + file.getOriginalFilename() +
                        "' muito grande. Máximo permitido: " + maxMB);
            }
        }
    }

    private byte[] resizeImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new IllegalArgumentException("Arquivo sem content-type.");

        // Formatos suportados pelo Thumbnailator
        boolean isThumbnailSupported =
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/bmp") ||
                        contentType.equals("image/gif");

        // WEBP e outros formatos que Thumbnailator não redimensiona
        boolean isGenericImage = contentType.startsWith("image/");

        // === 1. Se for PDF → RECUSAR (ou adaptar depois caso queira) ===
        if (contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("PDF não pode ser usado como imagem de produto.");
        }

        // === 2. Se não for imagem válida → bloquear ===
        if (!isThumbnailSupported && !isGenericImage) {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + contentType);
        }

        // === 3. Se for imagem pequena (< 800px), não redimensionar ===
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IllegalArgumentException("Não foi possível ler a imagem.");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        boolean precisaRedimensionar = width > 1500 || height > 1500;

        // === 4. Redimensiona somente imagens muito grandes ===
        if (isThumbnailSupported && precisaRedimensionar) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(original)
                    .size(1200, 1200)        // Maior resolução, menos perda de qualidade
                    .outputQuality(0.90)     // QUASE sem perda
                    .keepAspectRatio(true)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }

        // === 5. WEBP e imagens pequenas → retornar como estão ===
        return file.getBytes();
    }





    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCreatedAt(product.getCreatedAt().toLocalDateTime());
        dto.setUpdatedAt(product.getUpdatedAt().toLocalDateTime());

        // --- imagens do produto (product_image) ---
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> urls = product.getImages().stream()
                    .map(pi -> pi.getImageUrl())
                    .collect(Collectors.toList());
            dto.setImageUrls(urls);

            // tentar achar imagem principal por isMain (se existir), senão a primeira
            product.getImages().stream()
                    .filter(pi -> {
                        try {
                            // se existir o campo isMain
                            return (Boolean) pi.getClass().getMethod("isMain").invoke(pi);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .ifPresent(pi -> dto.setMainImageUrl(pi.getImageUrl()));

            if (dto.getMainImageUrl() == null && !urls.isEmpty()) {
                dto.setMainImageUrl(urls.get(0));
            }
        } else {
            dto.setImageUrls(List.of());
            dto.setMainImageUrl(null);
        }

        // --- subcategoria e categoria (herdada) ---
        SubCategory sub = product.getSubcategory();
        if (sub != null) {
            dto.setSubcategoryId(sub.getId());
            dto.setSubcategoryName(sub.getName());

            if (sub.getCategory() != null) {
                dto.setCategoryId(sub.getCategory().getId());
                dto.setCategoryName(sub.getCategory().getName());
            }
        }

        // --- temas (herdados via subcategoria) ---
        if (sub != null && sub.getThemes() != null && !sub.getThemes().isEmpty()) {
            dto.setThemeIds(sub.getThemes().stream()
                    .map(t -> t.getId())
                    .collect(Collectors.toList()));

            dto.setThemeNames(sub.getThemes().stream()
                    .map(t -> t.getName())
                    .collect(Collectors.toList()));
        } else {
            dto.setThemeIds(List.of());
            dto.setThemeNames(List.of());
        }

        return dto;
    }

    }

