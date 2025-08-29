package br.edu.fatecpg.BenucciArtesanato.record;

public record RegisterRequest(
        String nome,
        String email,
        String senha,
        String telefone,
        String endereco
) {}
