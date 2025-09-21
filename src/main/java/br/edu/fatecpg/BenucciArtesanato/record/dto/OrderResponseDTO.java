package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private String mpInitPoint; // link do checkout
    private String status;
    private String paymentStatus;
}
