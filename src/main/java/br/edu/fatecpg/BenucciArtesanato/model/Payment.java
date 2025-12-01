package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;
    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(nullable = false)
    private String status;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate = OffsetDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "init_point")
    private String initPoint;

    @Column(name = "sandbox_init_point")
    private String sandboxLink;
}
