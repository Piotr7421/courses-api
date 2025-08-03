package io.github.Piotr7421.courses.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.Piotr7421.courses.exception.IncompatibleTeacherLanguageException;
import io.github.Piotr7421.courses.model.Student;
import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.repository.StudentRepository;

import java.text.MessageFormat;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TeacherLanguageValidator {

    private final StudentRepository studentRepository;

    public void validateTeacherLanguages(Teacher teacher) {
        Set<Student> students = studentRepository.findAllByTeacherId(teacher.getId());
        students.forEach(student -> validateTeacherLanguage(teacher, student));
    }

    public void validateTeacherLanguage(Teacher teacher, Student student) {
        if (!teacher.getLanguages().contains(student.getLanguage())) {
            throw new IncompatibleTeacherLanguageException(MessageFormat
                    .format("Incompatible teacher language, teacher id={0}, language={1}",
                            teacher.getId(), student.getLanguage()));
        }
    }
}
