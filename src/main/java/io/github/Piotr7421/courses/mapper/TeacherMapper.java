package io.github.Piotr7421.courses.mapper;

import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.model.command.CreateTeacherCommand;
import io.github.Piotr7421.courses.model.dto.TeacherDto;

public class TeacherMapper {

    public static TeacherDto mapToDto(Teacher teacher) {
        return TeacherDto.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .languages(teacher.getLanguages())
                .build();
    }

    public static Teacher mapFromCommand(CreateTeacherCommand command) {
        return Teacher.builder()
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .languages(command.getLanguages())
                .active(true)
                .build();
    }
}
