package pl.spring.courses.exception;

public class LessonAlreadyStartedException extends RuntimeException {

    public LessonAlreadyStartedException(String message) {
        super(message);
    }
}
