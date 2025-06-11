package pl.spring.courses.exception;

public class StudentOptimisticLockException extends RuntimeException {

    public StudentOptimisticLockException(String message) {
        super(message);
    }
}
