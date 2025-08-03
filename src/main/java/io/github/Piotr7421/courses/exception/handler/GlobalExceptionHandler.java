package io.github.Piotr7421.courses.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.github.Piotr7421.courses.exception.DatabaseConstraintException;
import io.github.Piotr7421.courses.exception.IncompatibleTeacherLanguageException;
import io.github.Piotr7421.courses.exception.InvalidLanguageException;
import io.github.Piotr7421.courses.exception.LessonAlreadyStartedException;
import io.github.Piotr7421.courses.exception.LessonNotFoundException;
import io.github.Piotr7421.courses.exception.OverlappingLessonException;
import io.github.Piotr7421.courses.exception.StudentNotFoundException;
import io.github.Piotr7421.courses.exception.StudentOptimisticLockException;
import io.github.Piotr7421.courses.exception.TeacherHasStudentsException;
import io.github.Piotr7421.courses.exception.TeacherLockTimeoutException;
import io.github.Piotr7421.courses.exception.TeacherNotFoundException;
import io.github.Piotr7421.courses.exception.TeacherOptimisticLockException;
import io.github.Piotr7421.courses.exception.model.ExceptionDto;
import io.github.Piotr7421.courses.exception.model.ValidationErrorDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ValidationErrorDto errorDto = new ValidationErrorDto();
        exception.getFieldErrors().forEach(fieldError ->
                errorDto.addViolation(fieldError.getField(), fieldError.getDefaultMessage()));
        return errorDto;
    }

    @ExceptionHandler({
            DatabaseConstraintException.class,
            IncompatibleTeacherLanguageException.class,
            InvalidLanguageException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleSpecifiedExceptions(RuntimeException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler({
            TeacherNotFoundException.class,
            StudentNotFoundException.class,
            LessonNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleNotFoundExceptions(RuntimeException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler({
            TeacherOptimisticLockException.class,
            TeacherHasStudentsException.class,
            LessonAlreadyStartedException.class,
            TeacherLockTimeoutException.class,
            StudentOptimisticLockException.class,
            OverlappingLessonException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleSpecifiedOptimisticExceptions(RuntimeException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleJsonProcessingException(JsonProcessingException exception) {
        return new ExceptionDto(exception.getMessage());
    }
}
