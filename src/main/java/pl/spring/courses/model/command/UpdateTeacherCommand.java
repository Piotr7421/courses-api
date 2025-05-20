package pl.spring.courses.model.command;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pl.spring.courses.common.Language;

import java.util.Set;

@Getter
@Builder
@ToString
public class UpdateTeacherCommand {

    @NotEmpty(message = "EMPTY_VALUE")
    private Set<Language> languages;
}
