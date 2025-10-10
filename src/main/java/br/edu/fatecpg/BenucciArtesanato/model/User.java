package br.edu.fatecpg.BenucciArtesanato.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String cpf;
    private String phoneNumber;
    private String address;

    /**
     * 👇 Esse campo define o papel do usuário no Spring Security.
     * Valores esperados:
     * - "ROLE_USER"
     * - "ROLE_ADMIN"
     */
    @Column(nullable = false)
    private String role;

    /**
     * 👇 Campo virtual para compatibilidade com frontend
     * Retorna o role sem o prefixo "ROLE_"
     * Este campo é serializado como "type" no JSON
     */
    @JsonProperty("type")
    public String getType() {
        if (role == null) return "USER";
        return role.replace("ROLE_", "");
    }

    /**
     * 👇 Setter para type (para compatibilidade bidirecional)
     * Converte type em role com prefixo ROLE_
     */
    @JsonProperty("type")
    public void setType(String type) {
        if (type == null) {
            this.role = "ROLE_USER";
        } else if (type.startsWith("ROLE_")) {
            this.role = type;
        } else {
            this.role = "ROLE_" + type.toUpperCase();
        }
    }

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;
}
