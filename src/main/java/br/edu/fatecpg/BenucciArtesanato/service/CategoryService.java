package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;


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


    public CategoryDto createCategory(CategoryDto dto) {
        categoryRepository.findByName(dto.getName())
                .ifPresent(cat -> { throw new RuntimeException("Categoria já existe"); });

        Category category = Category.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .build();

        Category saved = categoryRepository.save(category);

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
