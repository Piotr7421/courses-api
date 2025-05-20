package pl.spring.courses.model.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class LessonDto {

    private int id;
    private LocalDateTime date;
    private TeacherDto teacher;
    private StudentDto student;
}
