package io.github.Piotr7421.courses.model.command;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import io.github.Piotr7421.courses.common.Language;

@Getter
@Builder
@ToString
public class CreateStudentCommand {

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String firstName;

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String lastName;

    @NotNull(message = "NULL_VALUE")
    @Enumerated(EnumType.STRING)
    private Language language;

    @Positive
    private int teacherId;
}
