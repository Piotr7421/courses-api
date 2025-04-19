package pl.spring.lessons.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonDto {

    private int id;
    private LocalDateTime date;
    private TeacherDto teacher;
    private StudentDto student;
}
