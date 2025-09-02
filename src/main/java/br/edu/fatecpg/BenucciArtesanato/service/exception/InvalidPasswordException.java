package br.edu.fatecpg.BenucciArtesanato.service.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}