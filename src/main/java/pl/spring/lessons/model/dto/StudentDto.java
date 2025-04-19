package pl.spring.lessons.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.spring.lessons.common.Language;

@Data
@Builder
public class StudentDto {

    private int id;
    private String firstName;
    private String lastName;
    private Language language;
}
