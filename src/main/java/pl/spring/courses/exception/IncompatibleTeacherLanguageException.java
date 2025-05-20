package pl.spring.courses.exception;

public class IncompatibleTeacherLanguageException extends RuntimeException {

    public IncompatibleTeacherLanguageException(String message) {
        super(message);
    }
}
