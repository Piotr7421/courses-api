package pl.spring.courses.exception;

public class TeacherLockTimeoutException extends RuntimeException {

    public TeacherLockTimeoutException(String message) {
        super(message);
    }
}
