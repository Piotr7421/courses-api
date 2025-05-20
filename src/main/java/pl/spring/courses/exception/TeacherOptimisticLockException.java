package pl.spring.courses.exception;

public class TeacherOptimisticLockException extends RuntimeException {

    public TeacherOptimisticLockException(String message) {
        super(message);
    }
}
