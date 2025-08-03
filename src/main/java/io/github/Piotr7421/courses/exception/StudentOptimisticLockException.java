package io.github.Piotr7421.courses.exception;

public class StudentOptimisticLockException extends RuntimeException {

    public StudentOptimisticLockException(String message) {
        super(message);
    }
}
