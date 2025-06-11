package pl.spring.courses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import pl.spring.courses.common.Language;
import pl.spring.courses.exception.DatabaseConstraintException;
import pl.spring.courses.exception.IncompatibleTeacherLanguageException;
import pl.spring.courses.exception.StudentNotFoundException;
import pl.spring.courses.exception.TeacherLockTimeoutException;
import pl.spring.courses.exception.TeacherNotFoundException;
import pl.spring.courses.exception.TeacherOptimisticLockException;
import pl.spring.courses.mapper.StudentMapper;
import pl.spring.courses.model.Student;
import pl.spring.courses.model.Teacher;
import pl.spring.courses.model.command.CreateStudentCommand;
import pl.spring.courses.model.command.UpdateStudentCommand;
import pl.spring.courses.model.dto.StudentDto;
import pl.spring.courses.repository.StudentRepository;
import pl.spring.courses.repository.TeacherRepository;
import pl.spring.courses.validator.TeacherLanguageValidator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherLanguageValidator teacherLanguageValidator;

    @InjectMocks
    private StudentService studentService;

    @Captor
    private ArgumentCaptor<Student> studentCaptor;

    private Teacher teacher;
    private Student student;
    private Student studentToSave;
    private StudentDto studentDto;
    private CreateStudentCommand createStudentCommand;
    private UpdateStudentCommand updateStudentCommand;
    private Teacher newTeacher;

    @BeforeEach
    void setUp() {
        int studentId = 1;
        int teacherId = 1;
        int newTeacherId = 2;
        teacher = Teacher.builder()
                .id(teacherId)
                .firstName("Anna")
                .lastName("Nowak")
                .languages(Set.of(Language.JAVA, Language.PYTHON))
                .active(true)
                .build();
        createStudentCommand = CreateStudentCommand.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .language(Language.JAVA)
                .teacherId(teacherId)
                .build();
        studentToSave = Student.builder()
                .id(0)
                .firstName(createStudentCommand.getFirstName())
                .lastName(createStudentCommand.getLastName())
                .language(createStudentCommand.getLanguage())
                .active(true)
                .build();
        student = Student.builder()
                .id(studentId)
                .firstName(createStudentCommand.getFirstName())
                .lastName(createStudentCommand.getLastName())
                .language(createStudentCommand.getLanguage())
                .teacher(teacher)
                .active(true)
                .build();
        updateStudentCommand = UpdateStudentCommand.builder()
                .teacherId(newTeacherId)
                .build();
        newTeacher = Teacher.builder()
                .id(newTeacherId)
                .firstName("Kinga")
                .lastName("Kowal")
                .languages(Set.of(Language.JAVA, Language.COBOL))
                .build();
        studentDto = StudentDto.builder()
                .id(studentId)
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .language(student.getLanguage())
                .teacherId(teacherId)
                .build();
    }

    @Test
    void findAll_ShouldReturnListOfStudents() {
        Student student1 = Student.builder()
                .teacher(new Teacher())
                .build();
        student.setTeacher(new Teacher());
        List<Student> students = List.of(student1, student);
        List<StudentDto> expectedDto = students.stream()
                .map(StudentMapper::mapToDto)
                .toList();
        when(studentRepository.findAll()).thenReturn(students);

        List<StudentDto> result = studentService.findAll();

        assertThat(expectedDto).isEqualTo(result);
        assertThat(result).hasSize(2);
        verify(studentRepository).findAll();
    }

    @Test
    void findAll_NoStudents_ShouldReturnEmptyList() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        List<StudentDto> result = studentService.findAll();

        assertThat(result).isEmpty();
        verify(studentRepository).findAll();
    }

    @Test
    void findById_ShouldReturnStudentDto() {
        int studentId = 1;
        Student student = Student.builder()
                .id(studentId)
                .teacher(new Teacher())
                .build();
        StudentDto expectedDto = StudentMapper.mapToDto(student);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        StudentDto result = studentService.findById(studentId);

        assertThat(expectedDto).isEqualTo(result);
        verify(studentRepository).findById(studentId);
    }

    @Test
    void findById_WhenStudentNotFound_ShouldThrowException() {
        int studentId = 1;
        String expectedExceptionMsg = MessageFormat.format("Student with id={0} not found", studentId);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(StudentNotFoundException.class)
                .isThrownBy(() -> studentService.findById(studentId))
                .withMessage(expectedExceptionMsg);

        verify(studentRepository).findById(studentId);
    }

    @Test
    void create_HappyPath_ShouldCreateStudent() {

        when(teacherRepository.findWithPessimisticLockingById(teacher.getId())).thenReturn(Optional.of(teacher));
        doNothing().when(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);
        when(studentRepository.saveAndFlush(studentToSave)).thenReturn(student);

        StudentDto result = studentService.create(createStudentCommand);

        assertThat(result.getId()).isEqualTo(studentDto.getId());

        verify(teacherRepository).findWithPessimisticLockingById(createStudentCommand.getTeacherId());
        verify(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);
        verify(studentRepository).saveAndFlush(studentCaptor.capture());

        Student capturedStudent = studentCaptor.getValue();
        assertEquals(capturedStudent.getFirstName(), createStudentCommand.getFirstName());
        assertEquals(capturedStudent.getLastName(), createStudentCommand.getLastName());
        assertEquals(capturedStudent.getLanguage(), createStudentCommand.getLanguage());
        assertEquals(capturedStudent.getTeacher().getId(), createStudentCommand.getTeacherId());
    }

    @Test
    void create_WhenTeacherNotFound_ShouldThrowTeacherNotFoundException() {
        int nonExistentTeacherId = 10;
        CreateStudentCommand commandWithNonExistentTeacher = CreateStudentCommand.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .language(Language.JAVA)
                .teacherId(nonExistentTeacherId)
                .build();
        String expectedExceptionMsg = MessageFormat
                .format("Teacher with id={0} not found", nonExistentTeacherId);
        when(teacherRepository.findWithPessimisticLockingById(nonExistentTeacherId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> studentService.create(commandWithNonExistentTeacher))
                .withMessage(expectedExceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(nonExistentTeacherId);
        verify(teacherLanguageValidator, never()).validateTeacherLanguage(any(Teacher.class), any(Student.class));
        verify(studentRepository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void create_WhenPessimisticLockingFailureException_ShouldThrowTeacherLockTimeoutException() {
        when(teacherRepository.findWithPessimisticLockingById(createStudentCommand.getTeacherId()))
                .thenThrow(new PessimisticLockingFailureException("JDBC exception executing SQL [select ... for update] [Lock wait timeout exceeded; try restarting transaction] [n/a]; SQL [n/a]"));

        assertThatExceptionOfType(TeacherLockTimeoutException.class)
                .isThrownBy(() -> studentService.create(createStudentCommand))
                .withMessage("Could not acquire lock on teacher - operation timed out");

        verify(teacherRepository).findWithPessimisticLockingById(createStudentCommand.getTeacherId());
        verify(teacherLanguageValidator, never()).validateTeacherLanguage(teacher, studentToSave);
        verify(studentRepository, never()).saveAndFlush(studentToSave);
    }

    @Test
    void create_WhenTeacherLanguageIsIncompatible_ShouldThrowIncompatibleTeacherLanguageException() {
        String expectedExceptionMsg = MessageFormat.format(
                "Incompatible teacher language, teacher id={0}, language={1}",
                teacher.getId(),
                studentToSave.getLanguage());
        when(teacherRepository.findWithPessimisticLockingById(createStudentCommand.getTeacherId())).thenReturn(Optional.of(teacher));
        doThrow(new IncompatibleTeacherLanguageException(expectedExceptionMsg))
                .when(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);

        assertThatExceptionOfType(IncompatibleTeacherLanguageException.class)
                .isThrownBy(() -> studentService.create(createStudentCommand))
                .withMessage(expectedExceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(createStudentCommand.getTeacherId());
        verify(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);
        verify(studentRepository, never()).saveAndFlush(any(Student.class));
    }

    @Test
    void create_WhenDataIntegrityViolationException_ShouldThrowDatabaseConstraintException() {
        when(teacherRepository.findWithPessimisticLockingById(createStudentCommand.getTeacherId())).thenReturn(Optional.of(teacher));
        doNothing().when(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);
        when(studentRepository.saveAndFlush(studentToSave))
                .thenThrow(new DataIntegrityViolationException("Database integrity constraint violation occurred"));

        assertThatExceptionOfType(DatabaseConstraintException.class)
                .isThrownBy(() -> studentService.create(createStudentCommand))
                .withMessage("Violation of integrity constraints while student insertion to the database");

        verify(teacherRepository).findWithPessimisticLockingById(createStudentCommand.getTeacherId());
        verify(teacherLanguageValidator).validateTeacherLanguage(teacher, studentToSave);
        verify(studentRepository).saveAndFlush(studentToSave);
    }

        @Test
    void update_HappyPath_ShouldUpdateStudent() {
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId())).thenReturn(Optional.of(newTeacher));
        when(studentRepository.findWithLockingById(student.getId())).thenReturn(Optional.of(student));
        doNothing().when(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);
        when(studentRepository.saveAndFlush(student)).thenReturn(student);
        when(studentRepository.saveAndFlush(student)).thenAnswer(invocation -> {
            Student savedStudent = invocation.getArgument(0);
            assertThat(savedStudent.getTeacher()).isEqualTo(newTeacher);
            return savedStudent;
        });

        StudentDto result = studentService.update(student.getId(), updateStudentCommand);

        assertThat(result.getTeacherId()).isEqualTo(newTeacher.getId());

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository).findWithLockingById(student.getId());
        verify(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);

        verify(studentRepository).saveAndFlush(studentCaptor.capture());
        Student capturedStudent = studentCaptor.getValue();
        assertThat(capturedStudent.getId()).isEqualTo(student.getId());
        assertThat(capturedStudent.getFirstName()).isEqualTo(student.getFirstName());
        assertThat(capturedStudent.getLastName()).isEqualTo(student.getLastName());
        assertThat(capturedStudent.getLanguage()).isEqualTo(student.getLanguage());
        assertThat(capturedStudent.getTeacher().getId()).isEqualTo(newTeacher.getId());
    }

    @Test
    void update_WhenTeacherNotFound_ShouldThrowTeacherNotFoundException() {
        String expectedExceptionMsg = MessageFormat.format("Teacher with id={0} not found", newTeacher.getId());
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> studentService.update(newTeacher.getId(), updateStudentCommand))
                .withMessage(expectedExceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository, never()).findWithLockingById(student.getId());
        verify(teacherLanguageValidator, never()).validateTeacherLanguage(newTeacher, student);
    }

    @Test
    void update_WhenPessimisticLockingFailureException_ShouldThrowTeacherLockTimeoutException() {
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId()))
                .thenThrow(new PessimisticLockingFailureException("JDBC exception executing SQL [select ... for update] [Lock wait timeout exceeded; try restarting transaction] [n/a]; SQL [n/a]"));

        assertThatExceptionOfType(TeacherLockTimeoutException.class)
                .isThrownBy(() -> studentService.update(student.getId(), updateStudentCommand))
                .withMessage("Could not acquire lock on teacher - operation timed out");

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository, never()).findWithLockingById(student.getId());
        verify(teacherLanguageValidator, never()).validateTeacherLanguage(newTeacher, student);
        verify(studentRepository, never()).saveAndFlush(student);
    }


    @Test
    void update_WhenStudentNotFound_ShouldThrowException() {
        student.setTeacher(newTeacher);
        String expectedExceptionMsg = MessageFormat.format("Student with id={0} not found", student.getId());
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId())).thenReturn(Optional.of(newTeacher));
        when(studentRepository.findWithLockingById(student.getId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(StudentNotFoundException.class)
                .isThrownBy(() -> studentService.update(student.getId(), updateStudentCommand))
                .withMessage(expectedExceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository).findWithLockingById(student.getId());
        verify(teacherLanguageValidator, never()).validateTeacherLanguage(newTeacher, student);
        verify(studentRepository, never()).saveAndFlush(student);
    }

    @Test
    void update_WhenTeacherLanguageIsIncompatible_ShouldThrowIncompatibleTeacherLanguageException() {
        String expectedExceptionMsg = MessageFormat.format(
                "Incompatible teacher language, teacher id={0}, language={1}",
                newTeacher.getId(),
                student.getLanguage());
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId())).thenReturn(Optional.of(newTeacher));
        when(studentRepository.findWithLockingById(student.getId())).thenReturn(Optional.of(student));
        doThrow(new IncompatibleTeacherLanguageException(expectedExceptionMsg))
                .when(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);

        assertThatExceptionOfType(IncompatibleTeacherLanguageException.class)
                .isThrownBy(() -> studentService.update(student.getId(), updateStudentCommand))
                .withMessage(expectedExceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository).findWithLockingById(student.getId());
        verify(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);
        verify(studentRepository, never()).saveAndFlush(student);
    }

    @Test
    void update_WhenDataIntegrityViolationException_ShouldThrowDatabaseConstraintException() {
        when(teacherRepository.findWithPessimisticLockingById(newTeacher.getId())).thenReturn(Optional.of(newTeacher));
        when(studentRepository.findWithLockingById(student.getId())).thenReturn(Optional.of(student));
        doNothing().when(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);
        when(studentRepository.saveAndFlush(student))
                .thenThrow(new DataIntegrityViolationException("Database integrity constraint violation occurred"));

        assertThatExceptionOfType(DatabaseConstraintException.class)
                .isThrownBy(() -> studentService.update(student.getId(), updateStudentCommand))
                .withMessage("Violation of integrity constraints while student update to the database");

        verify(teacherRepository).findWithPessimisticLockingById(newTeacher.getId());
        verify(studentRepository).findWithLockingById(student.getId());
        verify(teacherLanguageValidator).validateTeacherLanguage(newTeacher, student);
        verify(studentRepository).saveAndFlush(student);
    }

    @Test
    void delete_ShouldDeleteStudent() {
        int studentId = 1;
        doNothing().when(studentRepository).deleteById(studentId);

        studentService.deleteById(studentId);

        verify(studentRepository).deleteById(studentId);
    }
}













