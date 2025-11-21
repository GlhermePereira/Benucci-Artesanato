package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private Integer stock;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // --------------------------
    // FK -> Subcategoria
    // --------------------------
    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    private SubCategory subcategory;

    // --------------------------
    // IMAGENS (1:N)
    // --------------------------
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public void addImage(ProductImage img) {
        img.setProduct(this);
        this.images.add(img);
    }

    // --------------------------
    // TEMAS (N:M via product_theme)
    // --------------------------
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTheme> productThemes = new ArrayList<>();

    public void addTheme(ProductTheme pt) {
        pt.setProduct(this);
        this.productThemes.add(pt);
    }

    // --------------------------
    // Métodos auxiliares
    // --------------------------

    /** Retorna somente os IDs dos temas */
    @Transient
    public List<Long> getThemeIds() {
        return productThemes.stream()
                .map(pt -> pt.getTheme().getId())
                .toList();
    }

    /** Retorna somente os nomes dos temas */
    @Transient
    public List<String> getThemeNames() {
        return productThemes.stream()
                .map(pt -> pt.getTheme().getName())
                .toList();
    }
}
