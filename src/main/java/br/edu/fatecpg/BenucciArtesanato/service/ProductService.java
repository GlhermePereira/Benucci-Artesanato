package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.*;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.ProductPageDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UpdateProductDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


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
    @Autowired
    private ProductThemeRepository productThemeRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private SubcategoryThemeRepository subcategoryThemeRepository;
    @Transactional(readOnly = true)
    public ProductPageDTO getPaginatedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Busca produtos com subcategoria e categoria, e carrega imagens e temas
        Page<Product> productPage = productRepository.findAllPaginated(pageable);

        List<ProductDTO> dtos = productPage.getContent().stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setDescription(product.getDescription());
                    dto.setPrice(product.getPrice());
                    dto.setStock(product.getStock());

                    // Primeira imagem
                    dto.setMainImageUrl(
                            product.getImages().stream()
                                    .map(ProductImage::getImageUrl)
                                    .findFirst()
                                    .orElse(null)
                    );

                    // Subcategoria e categoria
                    dto.setSubcategoryId(product.getSubcategory().getId());
                    dto.setSubcategoryName(product.getSubcategory().getName());
                    dto.setCategoryId(product.getSubcategory().getCategory().getId());
                    dto.setCategoryName(product.getSubcategory().getCategory().getName());

                    // Temas
                    dto.setThemeIds(product.getThemeIds());
                    dto.setThemeNames(product.getThemeNames());

                    // Datas
                    dto.setCreatedAt(product.getCreatedAt().toLocalDateTime());
                    dto.setUpdatedAt(product.getUpdatedAt().toLocalDateTime());

                    return dto;
                })
                .toList();

        return new ProductPageDTO(
                dtos,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages(),
                productPage.getTotalElements()
        );
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
    public void createProduct(ProductDTO dto, List<MultipartFile> images) {

        // ----- 1) Validar subcategoria -----
        SubCategory subcategory = SubcategoryRepository.findById(dto.getSubcategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Subcategoria não encontrada"));

        // ----- 2) Criar produto -----
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSubcategory(subcategory);

        product = productRepository.save(product);

        // ----- 3) VALIDAR E SALVAR THEMES (product_theme) -----
        // ----- 3) SALVAR THEMES (product_theme) -----
        // ----- 3) SALVAR THEMES (product_theme) -----
        if (dto.getThemeIds() != null && !dto.getThemeIds().isEmpty()) {

            // buscar os temas permitidos para a subcategoria
            List<Long> allowedThemes = subcategoryThemeRepository
                    .findThemeIdsBySubcategoryId(dto.getSubcategoryId());

            for (Long themeId : dto.getThemeIds()) {

                if (!allowedThemes.contains(themeId)) {
                    throw new IllegalArgumentException(
                            "O tema ID " + themeId + " não é permitido para esta subcategoria");
                }

                // Buscar a entidade Theme para criar o relacionamento
                Theme theme = themeRepository.findById(themeId)
                        .orElseThrow(() -> new IllegalArgumentException("Tema não encontrado"));

                ProductTheme pt = new ProductTheme(product, theme); // usa o construtor que monta o id composto

                productThemeRepository.save(pt);
            }
        }



        // ----- 4) IMAGENS (product_image) -----
        if (images != null && !images.isEmpty()) {

            validateFiles(images); // você já tinha

            for (MultipartFile file : images) {

                byte[] processed;
                try {
                    processed = resizeImage(file); // como já usa
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String imageUrl = supabaseService.uploadImage(
                        file.getOriginalFilename(),
                        processed
                );

                ProductImage pi = new ProductImage();
                pi.setImageUrl(imageUrl);
                pi.setProduct(product);

                productImageRepository.save(pi);
            }
        }

    }



    @Transactional
    public Product updateProduct(Long id, UpdateProductDTO dto, List<MultipartFile> images) {


        // ----- 1) Buscar produto existente -----
        Product product = productRepository.findByIdFull(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getProductThemes());

        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        // ----- 2) Atualizar campos básicos -----
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());

        // ----- 3) Atualizar subcategoria -----
        if (dto.getSubcategoryId() != null) {
            SubCategory subcategory = SubcategoryRepository.findByIdWithCategory(dto.getSubcategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Subcategoria não encontrada"));

            product.setSubcategory(subcategory);


        }

        // ----- 4) Atualizar temas (product_theme) -----
        if (dto.getThemeIds() != null) {
            // Apaga temas antigos
            productThemeRepository.deleteAllByProductId(product.getId());

            // Salva novos temas
            List<Long> allowedThemes = subcategoryThemeRepository
                    .findThemeIdsBySubcategoryId(product.getSubcategory().getId());

            for (Long themeId : dto.getThemeIds()) {
                if (!allowedThemes.contains(themeId)) {
                    throw new IllegalArgumentException(
                            "O tema ID " + themeId + " não é permitido para esta subcategoria");
                }

                Theme theme = themeRepository.findById(themeId)
                        .orElseThrow(() -> new IllegalArgumentException("Tema não encontrado"));

                ProductTheme pt = new ProductTheme(product, theme);
                productThemeRepository.save(pt);
            }
        }

        // ----- 5) Atualizar imagens (product_image) -----
        if (images != null && !images.isEmpty()) {
            // Apaga imagens antigas
            productImageRepository.deleteAllByProductId(product.getId());

            // Valida arquivos
            validateFiles(images);

            // Salva novas imagens
            for (MultipartFile file : images) {
                byte[] processed;
                try {
                    processed = resizeImage(file);
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao processar a imagem " + file.getOriginalFilename(), e);
                }

                String imageUrl = supabaseService.uploadImage(file.getOriginalFilename(), processed);

                ProductImage pi = new ProductImage();
                pi.setImageUrl(imageUrl);
                pi.setProduct(product);
                productImageRepository.save(pi);
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
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IllegalArgumentException("Não foi possível ler a imagem.");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        boolean precisaRedimensionar = width > 1500 || height > 1500;

        // Detecta se é um formato suportado pelo Thumbnailator
        boolean thumbnailSupported = isThumbnailSupported(file);

        // Redimensiona apenas imagens grandes e suportadas
        if (thumbnailSupported && precisaRedimensionar) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(original)
                    .size(1200, 1200)
                    .keepAspectRatio(true)
                    .outputQuality(0.9)
                    .outputFormat(getFormat(file)) // especifica formato
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }

        // Para WebP e imagens pequenas → retorna como estão
        return file.getBytes();
    }

    private boolean isThumbnailSupported(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new IllegalArgumentException("Arquivo sem content-type.");

        // Formatos que Thumbnailator consegue processar
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/bmp") ||
                contentType.equals("image/gif");
    }

    private String getFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new IllegalArgumentException("Arquivo sem Content-Type.");

        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/bmp" -> "bmp";
            case "image/gif" -> "gif";
            default -> throw new IllegalArgumentException(
                    "Formato não suportado para redimensionamento: " + contentType
            );
        };
    }


    private static boolean isIsThumbnailSupported(MultipartFile file) {
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
        return isThumbnailSupported;
    }







    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public ProductDTO convertToDTO(Product product) {
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
                    .map(ProductImage::getImageUrl)
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
                    .map(Theme::getId)
                    .collect(Collectors.toList()));

            dto.setThemeNames(sub.getThemes().stream()
                    .map(Theme::getName)
                    .collect(Collectors.toList()));
        } else {
            dto.setThemeIds(List.of());
            dto.setThemeNames(List.of());
        }

        return dto;
    }

    }

