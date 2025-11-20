package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryInputDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategorySimpleDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // =============================
    // CATEGORY
    // =============================

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryDto)
                .toList();
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return mapToCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategorySimpleDTO> getSimpleCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategorySimpleDTO(c.getId(), c.getName()))
                .toList();
    }

    public CategoryDto createCategory(CategoryInputDTO dto) {

        categoryRepository.findByName(dto.name())
                .ifPresent(c -> {
                    throw new IllegalStateException("Categoria '" + dto.name() + "' já existe.");
                });

        Category category = Category.builder()
                .name(dto.name())
                .description(dto.description())
                .slug(SlugUtil.generateSlug(dto.name()))
                .build();

        return mapToCategoryDto(categoryRepository.save(category));
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSlug(dto.getSlug());

        return mapToCategoryDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada");
        }
        categoryRepository.deleteById(id);
    }

    // =============================
    // MAPPER
    // =============================

    private CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }
}
