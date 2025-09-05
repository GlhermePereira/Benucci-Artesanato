package br.edu.fatecpg.BenucciArtesanato.repository;


import br.edu.fatecpg.BenucciArtesanato.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Optional: custom method to find all products by category ID
    List<Product> findByCategoryId(Long categoryId);

}