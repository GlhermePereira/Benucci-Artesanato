package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String type; // cliente ou admin
    private String cpf;
}
