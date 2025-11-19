package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "review")
@IdClass(ReviewId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Review {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date")
    private OffsetDateTime reviewDate = OffsetDateTime.now();
}
