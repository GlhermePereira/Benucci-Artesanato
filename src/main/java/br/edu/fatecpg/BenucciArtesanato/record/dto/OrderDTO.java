package br.edu.fatecpg.BenucciArtesanato.record.dto;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String deliveryType;
    private String deliveryAddress;
    private String status;
    private List<OrderItemDTO> items;
}