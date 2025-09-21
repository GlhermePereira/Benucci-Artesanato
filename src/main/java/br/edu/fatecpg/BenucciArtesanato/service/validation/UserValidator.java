package br.edu.fatecpg.BenucciArtesanato.service.validation;

import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateRegisterRequest(RegisterRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (!request.email().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (request.password().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        if (!request.password().matches("^\\d{10,11}$")) {
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos numéricos");
        }

    }
}
