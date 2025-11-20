package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<SubCategory, Long> {

    // Busca subcategorias de uma categoria
    List<SubCategory> findByCategoryId(Long categoryId);

    // Busca por slug (pode repetir entre categorias, mas útil)
    Optional<SubCategory> findBySlug(String slug);

    // Busca por nome dentro de uma categoria específica
    Optional<SubCategory> findByCategoryIdAndName(Long categoryId, String name);

    // Busca por slug dentro de uma categoria (reflete UNIQUE(category_id, slug))
    Optional<SubCategory> findByCategoryIdAndSlug(Long categoryId, String slug);

    boolean existsByNameAndCategoryId(String name, Long categoryId);
}
