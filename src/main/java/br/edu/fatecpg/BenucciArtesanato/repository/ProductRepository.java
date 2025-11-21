package br.edu.fatecpg.BenucciArtesanato.repository;

import br.edu.fatecpg.BenucciArtesanato.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca todos os produtos ligados a uma Subcategoria específica.
     * Mapeamento feito via nome da coluna no Product (subcategory).
     */
    List<Product> findBySubcategoryId(Long subcategoryId);

    /**
     * Opcional: Busca customizada para buscar produtos por ID de Categoria.
     * Necessita JOIN para navegar de Product -> Subcategory -> Category.
     * * @param categoryId ID da Categoria.
     * @return Lista de produtos que pertencem àquela Categoria.
     */
    @Query("SELECT p FROM Product p JOIN p.subcategory s WHERE s.category.id = :categoryId")
    List<Product> findByCategoryId(Long categoryId);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.productThemes")
    Page<Product> findAllWithDetails(Pageable pageable);

    /**
     * Otimização de Performance (N+1): Carrega Produtos, Subcategorias, Categoria e Temas associados
     * em uma única consulta. Isso é crucial para o convertToDTO na Service.
     *
     * Nota: Esta query é mais complexa e assume que você tem um mapeamento
     * EAGER ou LAZY + o método getThemes() na Subcategory com um JOIN FETCH.
     * Se os temas forem carregados EAGERLY na Subcategory, este JOIN pode ser omitido.
     * * Assumindo que: Product -> Subcategory (EAGER/LAZY) e Subcategory -> Themes (LAZY, precisa de JOIN FETCH)
     */
    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.subcategory s " +
            "JOIN FETCH s.category c " +
            "LEFT JOIN FETCH s.themes t")
    List<Product> findAllWithSubcategoryAndThemes();

    @Query("""
       SELECT p FROM Product p
       JOIN FETCH p.subcategory s
       JOIN FETCH s.category
       WHERE p.id = :id
       """)
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
    @Query("""
SELECT DISTINCT p FROM Product p
JOIN FETCH p.subcategory s
JOIN FETCH s.category
LEFT JOIN FETCH s.themes
LEFT JOIN FETCH p.productThemes pt
LEFT JOIN FETCH pt.theme
LEFT JOIN FETCH p.images
WHERE p.id = :id
""")
    Optional<Product> findByIdFull(@Param("id") Long id);






}