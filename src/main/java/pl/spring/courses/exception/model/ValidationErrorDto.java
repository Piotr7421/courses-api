package pl.spring.courses.exception.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationErrorDto extends ExceptionDto {
    private final List<ViolationInfo> violations = new ArrayList<>();

    public ValidationErrorDto() {
        super("Validation error");
    }

    public void addViolation(String field, String message) {
        violations.add(new ViolationInfo(field, message));
    }

    public record ViolationInfo(String field, String message) {
    }
}
