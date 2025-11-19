package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String name;

    @Column(nullable = false, length = 160, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Relacionamento N:M → resolvido pela tabela subcategory_theme
    @ManyToMany(mappedBy = "themes", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubCategory> subcategories = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
