package pl.spring.courses.model.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.spring.courses.common.Language;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class StudentDto {

    private int id;
    private String firstName;
    private String lastName;
    private Language language;
    private int teacherId;
}
