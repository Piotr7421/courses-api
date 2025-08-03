package io.github.Piotr7421.courses.mapper;

import io.github.Piotr7421.courses.model.Lesson;
import io.github.Piotr7421.courses.model.command.CreateLessonCommand;
import io.github.Piotr7421.courses.model.dto.LessonDto;

public class LessonMapper {

    public static LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .date(lesson.getDate())
                .teacherId(lesson.getTeacher().getId())
                .studentId(lesson.getStudent().getId())
                .build();
    }

    public static Lesson mapFromCommand(CreateLessonCommand command) {
        return Lesson.builder()
                .date(command.getDate())
                .build();
    }
}
