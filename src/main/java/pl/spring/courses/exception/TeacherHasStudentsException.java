package pl.spring.courses.exception;

public class TeacherHasStudentsException extends RuntimeException {

    public TeacherHasStudentsException(String message) {
        super(message);
    }
}
