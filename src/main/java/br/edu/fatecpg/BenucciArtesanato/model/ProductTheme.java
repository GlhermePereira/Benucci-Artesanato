package br.edu.fatecpg.BenucciArtesanato.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "product_theme") // Mapeia para a tabela product_theme no banco
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTheme implements Serializable {

    // Define a Chave Primária como a classe ProductThemeId
    @EmbeddedId
    private ProductThemeId id;

    // Relacionamento ManyToOne para Produto
    // @MapsId mapeia o atributo 'productId' da chave composta para esta FK
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    @JsonIgnore // Evita loops infinitos de serialização JSON
    private Product product;

    // Relacionamento ManyToOne para Tema
    // @MapsId mapeia o atributo 'themeId' da chave composta para esta FK
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("themeId")
    @JoinColumn(name = "theme_id")
    private Theme theme;

    // Construtor utilitário para facilitar a criação de novos registros
    public ProductTheme(Product product, Theme theme) {
        this.product = product;
        this.theme = theme;
        // Inicializa a chave composta usando os IDs das entidades
        this.id = new ProductThemeId(product.getId(), theme.getId());
    }
}