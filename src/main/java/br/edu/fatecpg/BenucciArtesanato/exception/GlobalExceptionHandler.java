package br.edu.fatecpg.BenucciArtesanato.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Helper to add CORS headers to error responses so browser can read them
     */
    private HttpHeaders addCorsHeaders(WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        // Extract origin from request if available
        String origin = request.getHeader("Origin");
        if (origin != null && (
                origin.equals("http://localhost:3000") ||
                origin.equals("http://127.0.0.1:3000") ||
                origin.equals("http://192.168.1.198:3000") ||
                origin.equals("http://localhost:8081") ||
                origin.equals("http://192.168.1.198:8081") ||
                origin.startsWith("exp://")
        )) {
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "*");
        }
        return headers;
    }

    // Erros genéricos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .headers(addCorsHeaders(request))
                .body(error);
    }

    // Erros de JWT
    @ExceptionHandler({ JwtException.class, ExpiredJwtException.class })
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token inválido ou expirado",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .headers(addCorsHeaders(request))
                .body(error);
    }

    // Validação de argumentos (ex: @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce("", (a, b) -> a + b + "; ");
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                mensagem,
                request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(addCorsHeaders(request))
                .body(error);
    }

    // Erros específicos de negócio (exemplo: usuário não encontrado)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .headers(addCorsHeaders(request))
                .body(error);
    }

}
