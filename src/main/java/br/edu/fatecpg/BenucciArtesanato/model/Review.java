package br.edu.fatecpg.BenucciArtesanato.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuário que fez a review
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;


    // Nota de 1 a 5
    private Integer rating;

    // Comentário da review
    @Column(columnDefinition = "TEXT")
    private String comment;

    // Data da avaliação
    @Column(name = "review_date")
    private LocalDateTime reviewDate = LocalDateTime.now();
}
