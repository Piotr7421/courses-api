package pl.spring.lessons.mapper;

import pl.spring.lessons.model.Lesson;
import pl.spring.lessons.model.command.CreateLessonCommand;
import pl.spring.lessons.model.dto.LessonDto;

public class LessonMapper {

    public static LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .date(lesson.getDate())
                .teacher(TeacherMapper.mapToDto(lesson.getTeacher()))
                .student(StudentMapper.mapToDto(lesson.getStudent()))
                .build();
    }

    public static Lesson mapFromCommand(CreateLessonCommand command) {
        return Lesson.builder()
                .date(command.getDate())
                .build();
    }
}
