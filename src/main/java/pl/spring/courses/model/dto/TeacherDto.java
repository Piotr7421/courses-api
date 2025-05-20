package pl.spring.courses.model.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.spring.courses.common.Language;

import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class TeacherDto {

    private int id;
    private String firstName;
    private String lastName;
    private Set<Language> languages;
}
