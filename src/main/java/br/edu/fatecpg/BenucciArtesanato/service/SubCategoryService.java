package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.service.exception.CategoryNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.service.exception.DuplicateResourceException;
import br.edu.fatecpg.BenucciArtesanato.service.exception.SubcategoryNotFoundException;
import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryService {


    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;

    // =============================
// LISTAR POR CATEGORIA
// =============================
    public List<SubcategoryDto> getSubcategoriesByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Categoria com ID " + categoryId + " não encontrada.");
        }

        return subcategoryRepository.findByCategoryIdOrderByNameAsc(categoryId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =============================
// CRIAR SUBCATEGORY
// =============================
    public SubcategoryDto createSubcategory(Long categoryId, SubcategoryDto dto) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria com ID " + categoryId + " não encontrada."));

        if (subcategoryRepository.existsByNameAndCategoryId(dto.getName(), categoryId)) {
            throw new DuplicateResourceException(
                    "Já existe uma subcategoria com o nome '" + dto.getName() + "' nesta categoria."
            );
        }

        String slug = SlugUtil.generateSlug(dto.getName());

        subcategoryRepository.findByCategoryIdAndSlug(categoryId, slug)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Já existe uma subcategoria com nome/slug semelhante nesta categoria."
                    );
                });

        SubCategory sub = SubCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(slug)
                .category(category)
                .build();

        return mapToDto(subcategoryRepository.save(sub));
    }

    // =============================
// ATUALIZAR SUBCATEGORY
// =============================
    public SubcategoryDto updateSubcategory(Long categoryId, Long subcategoryId, SubcategoryDto dto) {

        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new SubcategoryNotFoundException("Subcategoria com ID " + subcategoryId + " não encontrada."));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("Subcategoria não pertence à categoria informada.");
        }

        subcategoryRepository.findByCategoryIdAndName(categoryId, dto.getName())
                .filter(existing -> !existing.getId().equals(subcategoryId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Já existe outra subcategoria com o nome '" + dto.getName() + "' nesta categoria."
                    );
                });

        sub.setName(dto.getName());
        sub.setDescription(dto.getDescription());
        sub.setSlug(SlugUtil.generateSlug(dto.getName()));

        return mapToDto(subcategoryRepository.save(sub));
    }

    // =============================
// DELETAR SUBCATEGORY
// =============================
    public void deleteSubcategory(Long categoryId, Long subcategoryId) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new SubcategoryNotFoundException("Subcategoria com ID " + subcategoryId + " não encontrada."));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("Subcategoria não pertence à categoria informada.");
        }

        subcategoryRepository.delete(sub);
    }

    // =============================
// MAPPER
// =============================
    private SubcategoryDto mapToDto(SubCategory sub) {
        return SubcategoryDto.builder()
                .id(sub.getId())
                .name(sub.getName())
                .slug(sub.getSlug())
                .description(sub.getDescription())
                .categoryId(sub.getCategory().getId())
                .build();
    }


}
