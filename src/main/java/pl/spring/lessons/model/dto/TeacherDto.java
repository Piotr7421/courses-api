package pl.spring.lessons.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.spring.lessons.common.Language;

import java.util.Set;

@Data
@Builder
public class TeacherDto {

    private int id;
    private String firstName;
    private String lastName;
    private Set<Language> languages;
}
