package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.ProductTheme;
import br.edu.fatecpg.BenucciArtesanato.model.ProductThemeId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductThemeRepository extends JpaRepository<ProductTheme, ProductThemeId> {

    // Lista todos os temas associados a um produto
    List<ProductTheme> findByIdProductId(Long productId);

    // Verifica se já existe o relacionamento produto-tema
    boolean existsById(ProductThemeId id);

    // Deleta todos os temas de um produto (útil se for atualizar)
    void deleteByIdProductId(Long productId);

    // Apenas lista só os IDs de theme vinculados
    @Query("SELECT pt.id.themeId FROM ProductTheme pt WHERE pt.id.productId = :productId")
    List<Long> findThemeIdsByProductId(Long productId);


    @Modifying
    @Transactional
    @Query("DELETE FROM ProductTheme pt WHERE pt.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);

}
