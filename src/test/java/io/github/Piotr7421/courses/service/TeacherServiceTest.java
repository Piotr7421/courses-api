package io.github.Piotr7421.courses.service;

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
import io.github.Piotr7421.courses.common.Language;
import io.github.Piotr7421.courses.exception.DatabaseConstraintException;
import io.github.Piotr7421.courses.exception.IncompatibleTeacherLanguageException;
import io.github.Piotr7421.courses.exception.TeacherHasStudentsException;
import io.github.Piotr7421.courses.exception.TeacherNotFoundException;
import io.github.Piotr7421.courses.exception.TeacherOptimisticLockException;
import io.github.Piotr7421.courses.mapper.TeacherMapper;
import io.github.Piotr7421.courses.model.Student;
import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.model.command.CreateTeacherCommand;
import io.github.Piotr7421.courses.model.command.UpdateTeacherCommand;
import io.github.Piotr7421.courses.model.dto.TeacherDto;
import io.github.Piotr7421.courses.repository.TeacherRepository;
import io.github.Piotr7421.courses.validator.TeacherLanguageValidator;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherLanguageValidator validator;

    @InjectMocks
    private TeacherService teacherService;

    @Captor
    private ArgumentCaptor<Teacher> teacherCaptor;

    private int teacherId;
    private Teacher teacher;
    private CreateTeacherCommand createTeacherCommand;
    private UpdateTeacherCommand updateTeacherCommand;
    private Teacher updatedTeacher;
    private Teacher teacherToSave;

    @BeforeEach
    void setUp() {

        teacherId = 1;
        teacher = Teacher.builder()
                .id(teacherId)
                .firstName("Jan")
                .lastName("Kowalski")
                .languages(Set.of(Language.JAVA))
                .build();
        createTeacherCommand = CreateTeacherCommand.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .languages(Set.of(Language.JAVA))
                .build();
        teacherToSave = Teacher.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .languages(Set.of(Language.JAVA))
                .build();
        updateTeacherCommand = UpdateTeacherCommand.builder()
                .languages(Set.of(Language.JAVA, Language.COBOL))
                .build();
        updatedTeacher = Teacher.builder()
                .id(teacherId)
                .firstName("Jan")
                .lastName("Kowalski")
                .languages(Set.of(Language.JAVA, Language.COBOL))
                .build();
    }

    @Test
    void findAll_ShouldReturnListOfTeachers() {
        List<Teacher> teachers = List.of(new Teacher(), new Teacher());
        List<TeacherDto> teachersDto = teachers.stream()
                .map(TeacherMapper::mapToDto)
                .toList();
        when(teacherRepository.findAll()).thenReturn(teachers);

        List<TeacherDto> result = teacherService.findAll();

        assertEquals(teachersDto, result);
        assertEquals(2, teachersDto.size());
        verify(teacherRepository).findAll();
    }

    @Test
    void findAll_NoTeachers_ShouldReturnEmptyList() {
        List<Teacher> teachers = List.of();
        List<TeacherDto> teachersDto = teachers.stream()
                .map(TeacherMapper::mapToDto)
                .toList();
        when(teacherRepository.findAll()).thenReturn(teachers);

        List<TeacherDto> result = teacherService.findAll();

        assertEquals(teachersDto, result);
        assertEquals(0, teachersDto.size());
        verify(teacherRepository).findAll();
    }

    @Test
    void findById_ShouldReturnTeacherDto() {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        TeacherDto teacherDto = TeacherMapper.mapToDto(teacher);

        TeacherDto result = teacherService.findById(teacherId);

        assertEquals(teacherDto, result);
        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void findById_ShouldThrowExceptionWhenTeacherNotFound() {
        String exceptionMsg = MessageFormat
                .format("Teacher with id={0} not found", teacherId);
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> teacherService.findById(teacherId))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void create_HappyPath_ShouldCreateTeacher() {

        TeacherDto teacherDto = TeacherMapper.mapToDto(teacher);
        when(teacherRepository.save(teacherToSave)).thenReturn(teacher);

        TeacherDto result = teacherService.create(createTeacherCommand);

        assertEquals(teacherDto, result);
        verify(teacherRepository).save(teacherToSave);
        verify(teacherRepository).save(teacherCaptor.capture());
        Teacher captorValue = teacherCaptor.getValue();
        assertEquals(captorValue.getFirstName(), createTeacherCommand.getFirstName());
        assertEquals(captorValue.getLastName(), createTeacherCommand.getLastName());
        assertEquals(captorValue.getLanguages(), createTeacherCommand.getLanguages());
    }

    @Test
    void create_WhenDataIntegrityViolationException_ShouldThrowDatabaseConstraintException() {
        when(teacherRepository.save(teacherToSave))
                .thenThrow(new DataIntegrityViolationException("Database integrity constraint violation occurred"));

        assertThatExceptionOfType(DatabaseConstraintException.class)
                .isThrownBy(() -> teacherService.create(createTeacherCommand))
                .withMessage("Violation of integrity constraints while teacher insertion to the database");

        verify(teacherRepository).save(teacherToSave);
    }

    @Test
    void update_HappyPath_ShouldUpdateTeacher() {

        TeacherDto expected = TeacherMapper.mapToDto(updatedTeacher);
        when(teacherRepository.findWithLockingById(teacherId)).thenReturn(Optional.of(teacher));
        doNothing().when(validator).validateTeacherLanguages(updatedTeacher);
        when(teacherRepository.saveAndFlush(updatedTeacher)).thenReturn(updatedTeacher);

        TeacherDto result = teacherService.update(teacherId, updateTeacherCommand);

        assertEquals(expected, result);
        verify(teacherRepository).findWithLockingById(teacherId);
        verify(validator).validateTeacherLanguages(updatedTeacher);
        verify(teacherRepository).saveAndFlush(updatedTeacher);
    }

    @Test
    void update_WhenLanguageValidationFails_ShouldThrowIncompatibleTeacherLanguageException() {
        when(teacherRepository.findWithLockingById(teacherId)).thenReturn(Optional.of(teacher));
        String exceptionMsg = "Incompatible teacher language, teacher id=1, language=CPP";
        doThrow(new IncompatibleTeacherLanguageException(exceptionMsg))
                .when(validator).validateTeacherLanguages(teacher);

        assertThatExceptionOfType(IncompatibleTeacherLanguageException.class)
                .isThrownBy(() -> teacherService.update(teacherId, updateTeacherCommand))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findWithLockingById(teacherId);
        verify(validator).validateTeacherLanguages(teacher);
        verify(teacherRepository, never()).save(updatedTeacher);
    }

    @Test
    void update_WhenDataIntegrityViolationException_ShouldThrowDatabaseConstraintException() {
        when(teacherRepository.findWithLockingById(teacherId)).thenReturn(Optional.of(teacher));
        doNothing().when(validator).validateTeacherLanguages(updatedTeacher);
        doThrow(new DataIntegrityViolationException("Database integrity constraint violation occurred"))
                .when(teacherRepository).saveAndFlush(updatedTeacher);

        assertThatExceptionOfType(DatabaseConstraintException.class)
                .isThrownBy(() -> teacherService.update(teacherId, updateTeacherCommand))
                .withMessage("Violation of integrity constraints while teacher update to the database");

        verify(teacherRepository).findWithLockingById(teacherId);
        verify(validator).validateTeacherLanguages(teacher);
        verify(teacherRepository, never()).save(updatedTeacher);
    }

    @Test
    void update_WhenOptimisticLockingFailureException_ShouldThrowTeacherOptimisticLockException() {
        when(teacherRepository.findWithLockingById(teacherId)).thenReturn(Optional.of(teacher));
        doNothing().when(validator).validateTeacherLanguages(updatedTeacher);
        doThrow(new OptimisticLockingFailureException("DB optimistic lock error"))
                .when(teacherRepository).saveAndFlush(updatedTeacher);

        assertThatExceptionOfType(TeacherOptimisticLockException.class)
                .isThrownBy(() -> teacherService.update(teacherId, updateTeacherCommand))
                .withMessage("Teacher was modified by another transaction");

        verify(teacherRepository).findWithLockingById(teacherId);
        verify(validator).validateTeacherLanguages(teacher);
        verify(teacherRepository, never()).save(updatedTeacher);
    }

    @Test
    void delete_WhenTheTeacherHasNoStudents_ShouldDeleteTheTeacher() {
        when(teacherRepository.findByIdWithStudents(teacherId)).thenReturn(Optional.of(teacher));
        doNothing().when(teacherRepository).deleteById(teacherId);

        teacherService.delete(teacherId);

        verify(teacherRepository).findByIdWithStudents(teacherId);
        verify(teacherRepository).deleteById(teacherId);
    }

    @Test
    void delete_WhenTeacherNotFound_ShouldThrowTeacherNotFoundException() {
        String exceptionMsg = MessageFormat
                .format("Teacher with id={0} not found", teacherId);
        when(teacherRepository.findByIdWithStudents(teacherId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> teacherService.delete(teacherId))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findByIdWithStudents(teacherId);
        verify(teacherRepository, never()).deleteById(teacherId);
    }

    @Test
    void delete_WhenTheTeacherHasStudents_ShouldThrowTeacherHasStudentsException() {
        teacher.setStudents(Set.of(new Student()));
        String exceptionMsg = MessageFormat
                .format("Teacher with id={0} has students.", teacherId);
        when(teacherRepository.findByIdWithStudents(teacherId)).thenReturn(Optional.of(teacher));

        assertThatExceptionOfType(TeacherHasStudentsException.class)
                .isThrownBy(() -> teacherService.delete(teacherId))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findByIdWithStudents(teacherId);
        verify(teacherRepository, never()).deleteById(teacherId);
    }
}