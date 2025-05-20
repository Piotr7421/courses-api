package pl.spring.courses.model.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pl.spring.courses.common.Language;

import java.util.Set;

@Getter
@Builder
@ToString
public class CreateTeacherCommand {

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String firstName;

    @NotNull(message = "NULL_VALUE")
    @Pattern(regexp = "[A-Z][a-z]{1,19}", message = "PATTERN_MISMATCH_{regexp}")
    private String lastName;

    @NotEmpty(message = "EMPTY_VALUE")
    private Set<Language> languages;
}
