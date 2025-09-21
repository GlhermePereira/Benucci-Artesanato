package br.edu.fatecpg.BenucciArtesanato.record.dto;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private Long userId;
    private String deliveryType; // pickup ou delivery
    private String deliveryAddress;
    private String paymentMethod; // Pix, Card, Mercado Pago
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long productId;
        private Integer quantity;
    }
}
