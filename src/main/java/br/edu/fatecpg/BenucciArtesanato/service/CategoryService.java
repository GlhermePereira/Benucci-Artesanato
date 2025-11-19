package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryInputDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategorySimpleDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ThemeRepository;
import br.edu.fatecpg.BenucciArtesanato.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ThemeRepository themeRepository;


    // =============================
    // CATEGORY
    // =============================

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllFetch();
        return categories.stream()
                .map(this::mapToCategoryDto)
                .toList();
    }


    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findByIdFetch(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return mapToCategoryDto(category);
    }
    @Transactional(readOnly = true)
    public List<CategorySimpleDTO> getSimpleCategories() {
        // 1. Busca todas as entidades Category
        return categoryRepository.findAll().stream()
                // 2. Mapeia cada entidade Category para o DTO simplificado
                .map(category -> new CategorySimpleDTO(category.getId(), category.getName()))

                .collect(Collectors.toList());
    }
    public CategoryDto createCategory(CategoryInputDTO dto) {
        // 1. Verifica a unicidade (case-insensitive)
        Optional<Category> existingCategory = categoryRepository.findByName(dto.name());
        if (existingCategory.isPresent()) {
            // Lança uma exceção de estado ilegal para ser capturada pelo ExceptionHandler
            throw new IllegalStateException("Categoria com o nome '" + dto.name() + "' já existe.");
        }

        // 2. Gera o slug a partir do nome
        String generatedSlug = SlugUtil.generateSlug(dto.name());

        // 3. Constrói e salva a entidade Category
        Category category = Category.builder()
                .name(dto.name())
                .description(dto.description())
                .slug(generatedSlug)
                // Não inicializa subcategories aqui.
                .build();

        Category saved = categoryRepository.save(category);

        // 4. Mapeia a entidade salva para o DTO de saída e retorna
        return mapToCategoryDto(saved);
    }



    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSlug(dto.getSlug());

        Category updated = categoryRepository.save(category);

        return mapToCategoryDto(updated);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada");
        }
        categoryRepository.deleteById(id);
    }


    // =============================
    // SUBCATEGORY
    // =============================

    public List<SubcategoryDto> getSubcategoriesByCategory(Long categoryId) {
        return subcategoryRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToSubcategoryDto)
                .collect(Collectors.toList());
    }

    public SubcategoryDto createSubcategory(Long categoryId, SubcategoryDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        SubCategory sub = SubCategory.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .category(category)
                .build();

        SubCategory saved = subcategoryRepository.save(sub);

        return mapToSubcategoryDto(saved);
    }

    public SubcategoryDto updateSubcategory(Long categoryId, Long subcategoryId, SubcategoryDto dto) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new RuntimeException("Subcategoria não pertence à categoria informada");
        }

        sub.setName(dto.getName());
        sub.setSlug(dto.getSlug());
        sub.setDescription(dto.getDescription());

        SubCategory updated = subcategoryRepository.save(sub);

        return mapToSubcategoryDto(updated);
    }

    public void deleteSubcategory(Long categoryId, Long subcategoryId) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new RuntimeException("Subcategoria não pertence à categoria");
        }

        subcategoryRepository.delete(sub);
    }


    // =============================
    // MAPPERS
    // =============================

    private CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .subcategories(
                        category.getSubcategories() == null ? null :
                                category.getSubcategories().stream()
                                        .map(this::mapToSubcategoryDto)
                                        .collect(Collectors.toList())
                )
                .build();
    }

    private SubcategoryDto mapToSubcategoryDto(SubCategory subcat) {
        return SubcategoryDto.builder()
                .id(subcat.getId())
                .name(subcat.getName())
                .slug(subcat.getSlug())
                .description(subcat.getDescription())
                .categoryId(subcat.getCategory().getId())
                .build();
    }
}
