package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Buscar categoria por nome (opcional)
    Optional<Category> findByName(String name);
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubcategories(Long id);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdFetch(@Param("id") Long id);
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subcategories")
    List<Category> findAllFetch();


}
