package pl.spring.courses.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.spring.courses.exception.DatabaseConstraintException;
import pl.spring.courses.exception.IncompatibleTeacherLanguageException;
import pl.spring.courses.exception.InvalidLanguageException;
import pl.spring.courses.exception.StudentNotFoundException;
import pl.spring.courses.exception.TeacherHasStudentsException;
import pl.spring.courses.exception.TeacherNotFoundException;
import pl.spring.courses.exception.TeacherOptimisticLockException;
import pl.spring.courses.exception.model.ExceptionDto;
import pl.spring.courses.exception.model.ValidationErrorDto;

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
            StudentNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleNotFoundExceptions(RuntimeException exception) {
        return new ExceptionDto(exception.getMessage());
    }

    @ExceptionHandler({TeacherOptimisticLockException.class,
            TeacherHasStudentsException.class})
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
