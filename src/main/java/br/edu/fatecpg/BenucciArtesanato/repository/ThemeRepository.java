package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    // Busca por nome (opcional, case-insensitive)
    Optional<Theme> findByName(String name);

    // Busca por slug (útil para URL amigável)
    Optional<Theme> findBySlug(String slug);

    // Lista todos os temas de uma subcategoria específica
    List<Theme> findBySubcategoriesId(Long subcategoryId);

    // Lista todos os temas ordenados por nome
    List<Theme> findAllByOrderByNameAsc();

    // Verifica se já existe um tema com esse nome
    boolean existsByName(String name);
}
