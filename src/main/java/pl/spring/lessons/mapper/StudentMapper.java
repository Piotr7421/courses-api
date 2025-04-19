package pl.spring.lessons.mapper;

import pl.spring.lessons.model.Student;
import pl.spring.lessons.model.dto.StudentDto;

public class StudentMapper {

    public static StudentDto mapToDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .language(student.getLanguage())
                .build();
    }
}
