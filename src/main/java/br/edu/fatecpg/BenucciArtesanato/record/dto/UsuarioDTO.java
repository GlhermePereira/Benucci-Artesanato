package br.edu.fatecpg.BenucciArtesanato.record.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String endereco;
    private String tipo; // cliente ou admin
}
