package pl.spring.courses.mapper;

import pl.spring.courses.model.Student;
import pl.spring.courses.model.command.CreateStudentCommand;
import pl.spring.courses.model.dto.StudentDto;

public class StudentMapper {

    public static StudentDto mapToDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .language(student.getLanguage())
                .teacherId(student.getTeacher().getId())
                .build();
    }

    public static Student mapFromCommand(CreateStudentCommand command) {
        return Student.builder()
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .language(command.getLanguage())
                .active(true)
                .build();
    }
}
