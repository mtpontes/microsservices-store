package br.com.ecommerce.auth.exception;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String METHOD_ARGUMENT_NOT_VALID_MESSAGE = "Input validation error";
    private final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleError404(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleError404(UserNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorMessage> handlerError401(InvalidTokenException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(), 
                ex.getMessage()
                )
            );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageWithFields> handleError400(MethodArgumentNotValidException ex) {
        var fields = ex.getFieldErrors().stream().collect(Collectors.toMap(f -> f.getField().toString(), f -> f.getDefaultMessage()));
        
        return ResponseEntity
            .badRequest()
            .body(new ErrorMessageWithFields(
                HttpStatus.BAD_REQUEST.value(),
                this.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
                fields
                )
            );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handlerErro400(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleError400(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), "Malformed or unexpected json format"));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorMessage> handleError400MissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest().body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage().split("for")[0]));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorMessage> handlerErro415(HttpMediaTypeNotSupportedException ex) {
        String unsupported = Optional.ofNullable(ex.getContentType())
            .map(e -> e.getType() + "/" + e.getSubtype())
            .orElse("unknown");

        String supported = ex.getSupportedMediaTypes().stream()
            .map(mediaType -> mediaType.getType() + "/" + mediaType.getSubtype())
            .collect(Collectors.joining(", "));
            
        String message = String.format("Unsupported media type '%s'. Supported media types are: %s", unsupported, supported);

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()).body(new ErrorMessage(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleError500(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
            .internalServerError()
            .body(new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                this.INTERNAL_SERVER_ERROR_MESSAGE
                )
            );
    }

    private record ErrorMessage(int status, Object error) {}
    private record ErrorMessageWithFields(int status, String error, Object fields) {}
}
