package io.github.Piotr7421.courses.exception;

public class TeacherOptimisticLockException extends RuntimeException {

    public TeacherOptimisticLockException(String message) {
        super(message);
    }
}
