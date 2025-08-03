package io.github.Piotr7421.courses.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.Piotr7421.courses.common.Language;
import io.github.Piotr7421.courses.exception.IncompatibleTeacherLanguageException;
import io.github.Piotr7421.courses.model.Student;
import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.repository.StudentRepository;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherLanguageValidatorTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private TeacherLanguageValidator validator;

    private Teacher teacher;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder()
                .id(1)
                .firstName("Anna")
                .lastName("Nowak")
                .build();
        student1 = Student.builder()
                .id(1)
                .firstName("Jan")
                .lastName("Kowalski")
                .teacher(teacher)
                .build();
        student2 = Student.builder()
                .id(2)
                .firstName("Bartek")
                .lastName("Kowal")
                .teacher(teacher)
                .build();
    }

    @Test
    void validateTeacherLanguage_WhenTeacherLanguagesContainStudentOne_ShouldNotThrowException() {
        teacher.setLanguages(Set.of(Language.JAVA, Language.JS));
        student1.setLanguage(Language.JAVA);

        assertThatCode(() -> validator.validateTeacherLanguage(teacher, student1))
                .doesNotThrowAnyException();
    }

    @Test
    void validateTeacherLanguage_WhenTeacherLanguagesDoNotContainStudentOne_ShouldThrowException() {
        teacher.setLanguages(Set.of(Language.PYTHON, Language.JS));
        student1.setLanguage(Language.JAVA);

        assertThatExceptionOfType(IncompatibleTeacherLanguageException.class)
                .isThrownBy(() -> validator.validateTeacherLanguage(teacher, student1))
                .withMessage("Incompatible teacher language, teacher id=1, language=JAVA");
    }

    @Test
    void validateTeacherLanguages_WhenTeacherLanguagesContainStudentsOnes_ShouldNotThrowException() {
        teacher.setLanguages(Set.of(Language.JAVA, Language.PYTHON, Language.JS));
        student1.setLanguage(Language.JAVA);
        student2.setLanguage(Language.PYTHON);
        Set<Student> students = Set.of(student1, student2);
        when(studentRepository.findAllByTeacherId(teacher.getId())).thenReturn(students);

        assertThatCode(() -> validator.validateTeacherLanguages(teacher))
                .doesNotThrowAnyException();

        verify(studentRepository).findAllByTeacherId(teacher.getId());
    }

    @Test
    void validateTeacherLanguages_WhenTeacherLanguagesDoNotContainStudentsOnes_ShouldThrowException() {
        teacher.setLanguages(Set.of(Language.JAVA, Language.PYTHON));
        student1.setLanguage(Language.JAVA);
        student2.setLanguage(Language.C);
        Set<Student> students = Set.of(student1, student2);
        when(studentRepository.findAllByTeacherId(teacher.getId())).thenReturn(students);

        assertThatExceptionOfType(IncompatibleTeacherLanguageException.class)
                .isThrownBy(() -> validator.validateTeacherLanguages(teacher))
                .withMessageContaining("Incompatible teacher language")
                .withMessageContaining("teacher id=1")
                .withMessageContaining("language=C");

        verify(studentRepository).findAllByTeacherId(teacher.getId());
    }

    @Test
    void validateTeacherLanguages_WhenNoStudents_ShouldNotThrowException() {
        teacher.setLanguages(Set.of(Language.JAVA));
        when(studentRepository.findAllByTeacherId(teacher.getId())).thenReturn(Collections.emptySet());

        assertThatCode(() -> validator.validateTeacherLanguages(teacher))
                .doesNotThrowAnyException();

        verify(studentRepository).findAllByTeacherId(teacher.getId());
    }
}