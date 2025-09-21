package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import br.edu.fatecpg.BenucciArtesanato.record.dto.CategoryDto;
import br.edu.fatecpg.BenucciArtesanato.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Listar todas as categorias
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // Criar nova categoria
    public CategoryDto createCategory(CategoryDto dto) {
        // Verifica se já existe categoria com o mesmo nome
        categoryRepository.findByName(dto.getName())
                .ifPresent(cat -> { throw new RuntimeException("Categoria já existe"); });

        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .build();
    }
}
