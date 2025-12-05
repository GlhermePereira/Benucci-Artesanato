package br.edu.fatecpg.BenucciArtesanato.service.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email já cadastrado: " + email);
    }
}
