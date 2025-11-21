package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.SubCategory;
import br.edu.fatecpg.BenucciArtesanato.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryThemeRepository extends JpaRepository<SubCategory, Long> {

    @Modifying
    @Query(value = "DELETE FROM subcategory_theme WHERE subcategory_id = :subcategoryId", nativeQuery = true)
    void deleteThemesBySubcategoryId(Long subcategoryId);

    @Query(value = "SELECT theme_id FROM subcategory_theme WHERE subcategory_id = :subcategoryId", nativeQuery = true)
    List<Long> findThemeIdsBySubcategoryId(Long subcategoryId);

}
