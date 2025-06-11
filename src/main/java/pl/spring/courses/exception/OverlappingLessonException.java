package pl.spring.courses.exception;

public class OverlappingLessonException extends RuntimeException {

    public OverlappingLessonException(String message) {
        super(message);
    }
}
