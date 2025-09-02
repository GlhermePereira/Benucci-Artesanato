package br.edu.fatecpg.BenucciArtesanato.service.validation;

import br.edu.fatecpg.BenucciArtesanato.record.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateRegisterRequest(RegisterRequest request) {
        if (request.nome() == null || request.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (!request.email().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (request.senha().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        if (!request.telefone().matches("^\\d{10,11}$")) {
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos numéricos");
        }
        if (request.endereco() == null || request.endereco().isBlank()) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }
    }
}
