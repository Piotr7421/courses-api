package pl.spring.courses.exception;

public class InvalidLanguageException extends RuntimeException {

    public InvalidLanguageException(String message) {
        super(message);
    }
}
