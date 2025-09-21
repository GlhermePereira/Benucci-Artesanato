package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateOrderStatusDTO {
    private String status; // "pending", "approved", "declined"
}
