package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"order\"")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK obrigatória (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Timestamp do banco (default CURRENT_TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Também existe no banco → deve estar na entidade
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "delivery_type")
    private String deliveryType;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.pending;

    @ToString.Exclude
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @ToString.Exclude
    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = true
    )
    private Payment payment;

    public enum OrderStatus {
        pending, preparing, shipped, delivered, canceled;

        public static OrderStatus fromString(String status) {
            for (OrderStatus s : values()) {
                if (s.name().equalsIgnoreCase(status)) return s;
            }
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
}