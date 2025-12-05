package br.edu.fatecpg.BenucciArtesanato.service.validation;

import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {
    public void validateRegister(RegisterRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Nome do usuário não pode ser vazio.");
        }

        if (request.email() == null || request.email().isBlank() || !isValidEmail(request.email())) {
            throw new IllegalArgumentException("Email inválido.");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres.");
        }

        if (request.cpf() == null || request.cpf().isBlank() || !isValidCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Número de telefone não pode ser vazio.");
        }

        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("Endereço não pode ser vazio.");
        }

        // Se precisar validar role
        if (request.role() != null && !request.role().equalsIgnoreCase("user") && !request.role().equalsIgnoreCase("admin")) {
            throw new IllegalArgumentException("Role inválida. Deve ser 'user' ou 'admin'.");
        }
    }

    // Regex simples para validar email
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Exemplo simples de validação de CPF (apenas formato)
    private boolean isValidCpf(String cpf) {
        return cpf.matches("\\d{11}");
    }
}
