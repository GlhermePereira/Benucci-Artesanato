package br.edu.fatecpg.BenucciArtesanato.service.exception;


public class SubcategoryNotFoundException extends RuntimeException {


    public SubcategoryNotFoundException() {
        super();
    }

    public SubcategoryNotFoundException(String message) {
        super(message);
    }

    public SubcategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubcategoryNotFoundException(Throwable cause) {
        super(cause);
    }

    protected SubcategoryNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
