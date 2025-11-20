package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Essencial para Chaves Compostas no JPA
public class ProductThemeId implements Serializable {

    // Deve ser do mesmo tipo do ID da entidade Product (Long)
    private Long productId;

    // Deve ser do mesmo tipo do ID da entidade Theme (Long)
    private Long themeId;
}