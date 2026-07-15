package br.com.ecommerce.auth.exception;

public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT = "User not found";

    public UserNotFoundException(Throwable throwable) {
        super(DEFAULT, throwable);
    }
}
