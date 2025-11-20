package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.record.dto.SubcategoryDto;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.SubcategoryRepository;
import br.edu.fatecpg.BenucciArtesanato.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubCategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;

    // =============================
    // LISTAR POR CATEGORIA
    // =============================

    public List<SubcategoryDto> getSubcategoriesByCategory(Long categoryId) {
        return subcategoryRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =============================
    // CRIAR SUBCATEGORY
    // =============================

    public SubcategoryDto createSubcategory(Long categoryId, SubcategoryDto dto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        SubCategory sub = SubCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .slug(SlugUtil.generateSlug(dto.getName()))
                .category(category)
                .build();

        return mapToDto(subcategoryRepository.save(sub));
    }

    // =============================
    // ATUALIZAR SUBCATEGORY
    // =============================

    public SubcategoryDto updateSubcategory(Long categoryId, Long subcategoryId, SubcategoryDto dto) {
        SubCategory sub = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new RuntimeException("Subcategoria não pertence à categoria informada");
        }

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
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new RuntimeException("Subcategoria não pertence à categoria informada");
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
