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

        // 🔎 Verifica se já existe subcategoria com o mesmo nome dentro da categoria
        if (subcategoryRepository.existsByNameAndCategoryId(dto.getName(), categoryId)) {
            throw new RuntimeException("Já existe uma subcategoria com este nome nesta categoria.");
        }

        // 🔎 Gera slug automaticamente
        String slug = SlugUtil.generateSlug(dto.getName());

        // 🔎 Verifica se slug já existe dentro desta categoria (caso nomes parecidos)
        subcategoryRepository.findByCategoryIdAndSlug(categoryId, slug)
                .ifPresent(existing -> {
                    throw new RuntimeException("Já existe uma subcategoria com nome/slug semelhante nesta categoria.");
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
                .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));

        // Verifica se pertence à categoria informada
        if (!sub.getCategory().getId().equals(categoryId)) {
            throw new RuntimeException("Subcategoria não pertence à categoria informada");
        }

        // Verifica duplicação dentro da mesma categoria
        subcategoryRepository.findByCategoryIdAndName(categoryId, dto.getName())
                .filter(existing -> !existing.getId().equals(subcategoryId))  // evita comparar consigo mesma
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Já existe outra subcategoria com o nome '" + dto.getName() + "' nesta categoria."
                    );
                });

        // Atualiza atributos
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
