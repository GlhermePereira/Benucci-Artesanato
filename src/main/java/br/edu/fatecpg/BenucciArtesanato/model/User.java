package br.edu.fatecpg.BenucciArtesanato.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"user\"") // obrigatório por causa do nome reservado
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password; // hash da password
    @Column(name = "phone_number")
    private String phoneNumber;
    private String address;
    private String type; // cliente/admin
}
