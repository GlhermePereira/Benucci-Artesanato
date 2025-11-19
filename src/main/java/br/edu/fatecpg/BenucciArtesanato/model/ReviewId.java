package br.edu.fatecpg.BenucciArtesanato.model;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewId implements Serializable {
    private Long user;
    private Long product;
}
