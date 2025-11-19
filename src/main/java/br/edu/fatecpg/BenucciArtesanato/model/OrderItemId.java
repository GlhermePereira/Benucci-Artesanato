package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemId implements Serializable {

    private Long orderId;
    private Long productId;
}
