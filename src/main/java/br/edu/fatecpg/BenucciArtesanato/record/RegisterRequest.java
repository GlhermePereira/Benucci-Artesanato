package br.edu.fatecpg.BenucciArtesanato.record;

public record RegisterRequest(
        String name,
        String email,
        String password,
        String phoneNumber,
        String address,
        String cpf,
        String role // Changed from 'role' to 'type' to match frontend's RegisterRequest
) {}
