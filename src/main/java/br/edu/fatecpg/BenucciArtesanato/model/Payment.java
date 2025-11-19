package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada pedido possui apenas 1 pagamento
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // Ex: 'Mercado Pago', 'Pix', 'Card'
    @Column(name = "payment_method")
    private String paymentMethod;

    // Usado somente quando o pagamento é via Mercado Pago
    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    // 'pending', 'approved', 'declined'
    @Column(nullable = false)
    private String status;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate = OffsetDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
}
