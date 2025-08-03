package io.github.Piotr7421.courses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import io.github.Piotr7421.courses.exception.LessonAlreadyStartedException;
import io.github.Piotr7421.courses.exception.LessonNotFoundException;
import io.github.Piotr7421.courses.exception.OverlappingLessonException;
import io.github.Piotr7421.courses.exception.StudentNotFoundException;
import io.github.Piotr7421.courses.exception.TeacherLockTimeoutException;
import io.github.Piotr7421.courses.exception.TeacherNotFoundException;
import io.github.Piotr7421.courses.mapper.LessonMapper;
import io.github.Piotr7421.courses.model.Lesson;
import io.github.Piotr7421.courses.model.Student;
import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.model.command.CreateLessonCommand;
import io.github.Piotr7421.courses.model.command.UpdateLessonCommand;
import io.github.Piotr7421.courses.model.dto.LessonDto;
import io.github.Piotr7421.courses.repository.LessonRepository;
import io.github.Piotr7421.courses.repository.StudentRepository;
import io.github.Piotr7421.courses.repository.TeacherRepository;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private LessonService lessonService;

    @Captor
    private ArgumentCaptor<Lesson> lessonCaptor;

    private int lessonId;
    private int teacherId;
    private int studentId;
    private Teacher teacher;
    private Student student;
    private Lesson lesson;
    private CreateLessonCommand createLessonCommand;
    private UpdateLessonCommand updateLessonCommand;
    private LessonDto lessonDto;

    @BeforeEach
    void setUp() {
        lessonId = 1;
        teacherId = 1;
        studentId = 1;
        LocalDateTime lessonDate = LocalDateTime.now().plusDays(1);
        teacher = Teacher.builder()
                .id(teacherId)
                .build();
        student = Student.builder()
                .id(studentId)
                .build();
        lesson = Lesson.builder()
                .id(lessonId)
                .date(lessonDate)
                .teacher(teacher)
                .student(student)
                .build();
        createLessonCommand = CreateLessonCommand.builder()
                .date(lessonDate)
                .teacherId(teacherId)
                .studentId(studentId)
                .build();
        updateLessonCommand = UpdateLessonCommand.builder()
                .date(lessonDate.plusDays(1))
                .build();
        lessonDto = LessonMapper.mapToDto(lesson);
    }

    @Test
    void findAll_ShouldReturnListOfLessons() {
        Lesson lesson1 = Lesson.builder()
                .teacher(new Teacher())
                .student(new Student())
                .build();
        List<Lesson> lessons = List.of(lesson1, lesson);
        List<LessonDto> expected = lessons.stream()
                .map(LessonMapper::mapToDto)
                .toList();
        when(lessonRepository.findAll()).thenReturn(lessons);

        List<LessonDto> result = lessonService.findAll();

        assertThat(result).isEqualTo(expected);
        assertThat(result).hasSize(2);
        verify(lessonRepository).findAll();
    }

    @Test
    void findAll_NoLessons_ShouldReturnEmptyList() {
        when(lessonRepository.findAll()).thenReturn(Collections.emptyList());

        List<LessonDto> result = lessonService.findAll();

        assertThat(result).isEmpty();
        verify(lessonRepository).findAll();
    }

    @Test
    void findById_ShouldReturnLessonDto() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        LessonDto result = lessonService.findById(lessonId);

        assertThat(result).isEqualTo(lessonDto);
        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void findById_WhenLessonNotFound_ShouldThrowException() {
        String exceptionMsg = MessageFormat
                .format("Lesson with id={0} not found", lessonId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(LessonNotFoundException.class)
                .isThrownBy(() -> lessonService.findById(lessonId))
                .withMessage(exceptionMsg);

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void save_HappyPath_ShouldCreateLesson() {
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.of(teacher));
        when(lessonRepository.existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        LessonDto result = lessonService.save(createLessonCommand);

        assertThat(result.getId()).isEqualTo(lessonDto.getId());

        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository).existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(studentRepository).findById(studentId);
        verify(lessonRepository).save(lessonCaptor.capture());
        Lesson captured = lessonCaptor.getValue();
        assertThat(captured.getDate()).isEqualTo(createLessonCommand.getDate());
        assertThat(captured.getTeacher().getId()).isEqualTo(createLessonCommand.getTeacherId());
        assertThat(captured.getStudent().getId()).isEqualTo(createLessonCommand.getStudentId());
    }

    @Test
    void save_WhenTeacherNotFound_ShouldThrowTeacherNotFoundException() {
        int nonExistentTeacherId = 10;
        CreateLessonCommand cmd = CreateLessonCommand.builder()
                .date(LocalDateTime.now().plusDays(1))
                .teacherId(nonExistentTeacherId)
                .studentId(studentId)
                .build();
        String exceptionMsg = MessageFormat
                .format("Teacher with id={0} not found", nonExistentTeacherId);
        when(teacherRepository.findWithPessimisticLockingById(nonExistentTeacherId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> lessonService.save(cmd))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(nonExistentTeacherId);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void save_WhenPessimisticLockingFailureException_ShouldThrowTeacherLockTimeoutException() {
        when(teacherRepository.findWithPessimisticLockingById(teacherId))
                .thenThrow(new PessimisticLockingFailureException("DB lock"));

        assertThatExceptionOfType(TeacherLockTimeoutException.class)
                .isThrownBy(() -> lessonService.save(createLessonCommand))
                .withMessage("Could not acquire lock on teacher - operation timed out");

        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void save_WhenOverlappingLesson_ShouldThrowOverlappingLessonException() {
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.of(teacher));
        when(lessonRepository.existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatExceptionOfType(OverlappingLessonException.class)
                .isThrownBy(() -> lessonService.save(createLessonCommand))
                .withMessage("Lesson overlaps with another lesson for the same teacher.");

        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository).existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(studentRepository, never()).findById(studentId);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void save_WhenStudentNotFound_ShouldThrowStudentNotFoundException() {
        String exceptionMsg = MessageFormat
                .format("Student with id={0} not found", studentId);
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.of(teacher));
        when(lessonRepository.existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(StudentNotFoundException.class)
                .isThrownBy(() -> lessonService.save(createLessonCommand))
                .withMessage(exceptionMsg);

        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(studentRepository).findById(studentId);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void update_HappyPath_ShouldUpdateLesson() {
        when(lessonRepository.findWithLockingById(lessonId)).thenReturn(Optional.of(lesson));
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.of(teacher));
        when(lessonRepository.existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(lessonRepository.saveAndFlush(lesson)).thenAnswer(invocation -> invocation.getArgument(0));

        LessonDto result = lessonService.update(lessonId, updateLessonCommand);

        assertThat(result.getDate()).isEqualTo(updateLessonCommand.getDate());
        verify(lessonRepository).findWithLockingById(lessonId);
        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository).existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(lessonRepository).saveAndFlush(lesson);
    }

    @Test
    void update_WhenLessonNotFound_ShouldThrowLessonNotFoundException() {
        String exceptionMsg = MessageFormat
                .format("Lesson with id={0} not found", lessonId);
        when(lessonRepository.findWithLockingById(lessonId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(LessonNotFoundException.class)
                .isThrownBy(() -> lessonService.update(lessonId, updateLessonCommand))
                .withMessage(exceptionMsg);

        verify(lessonRepository).findWithLockingById(lessonId);
        verify(teacherRepository, never()).findWithPessimisticLockingById(any(Integer.class));
    }

    @Test
    void update_WhenTeacherNotFound_ShouldThrowTeacherNotFoundException() {
        String exceptionMsg = MessageFormat
                .format("Teacher with id={0} not found", teacherId);
        when(lessonRepository.findWithLockingById(lessonId)).thenReturn(Optional.of(lesson));
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TeacherNotFoundException.class)
                .isThrownBy(() -> lessonService.update(lessonId, updateLessonCommand))
                .withMessage(exceptionMsg);

        verify(lessonRepository).findWithLockingById(lessonId);
        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository, never()).saveAndFlush(any(Lesson.class));
    }

    @Test
    void update_WhenTeacherLockTimeout_ShouldThrowTeacherLockTimeoutException() {
        when(lessonRepository.findWithLockingById(lessonId)).thenReturn(Optional.of(lesson));
        when(teacherRepository.findWithPessimisticLockingById(teacherId))
                .thenThrow(new PessimisticLockingFailureException("DB lock"));

        assertThatExceptionOfType(TeacherLockTimeoutException.class)
                .isThrownBy(() -> lessonService.update(lessonId, updateLessonCommand))
                .withMessage("Could not acquire lock on teacher - operation timed out");

        verify(lessonRepository).findWithLockingById(lessonId);
        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository, never()).saveAndFlush(any(Lesson.class));
    }

    @Test
    void update_WhenOverlappingLesson_ShouldThrowOverlappingLessonException() {
        when(lessonRepository.findWithLockingById(lessonId)).thenReturn(Optional.of(lesson));
        when(teacherRepository.findWithPessimisticLockingById(teacherId)).thenReturn(Optional.of(teacher));
        when(lessonRepository.existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatExceptionOfType(OverlappingLessonException.class)
                .isThrownBy(() -> lessonService.update(lessonId, updateLessonCommand))
                .withMessage("Lesson overlaps with another lesson for the same teacher.");

        verify(lessonRepository).findWithLockingById(lessonId);
        verify(teacherRepository).findWithPessimisticLockingById(teacherId);
        verify(lessonRepository).existsByTeacherAndDateAfterAndDateBefore(any(Teacher.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(lessonRepository, never()).saveAndFlush(any(Lesson.class));
    }

    @Test
    void delete_ShouldDeleteLesson() {
        when(lessonRepository.existsByIdAndDateBefore(eq(lessonId), any(LocalDateTime.class))).thenReturn(false);
        doNothing().when(lessonRepository).deleteById(lessonId);

        lessonService.deleteById(lessonId);

        verify(lessonRepository).existsByIdAndDateBefore(eq(lessonId), any(LocalDateTime.class));
        verify(lessonRepository).deleteById(lessonId);
    }

    @Test
    void delete_WhenLessonAlreadyStarted_ShouldThrowLessonAlreadyStartedException() {
        when(lessonRepository.existsByIdAndDateBefore(eq(lessonId), any(LocalDateTime.class))).thenReturn(true);

        assertThatExceptionOfType(LessonAlreadyStartedException.class)
                .isThrownBy(() -> lessonService.deleteById(lessonId))
                .withMessage("Deletion of a started lesson is forbidden");

        verify(lessonRepository).existsByIdAndDateBefore(eq(lessonId), any(LocalDateTime.class));
        verify(lessonRepository, never()).deleteById(lessonId);
    }
}