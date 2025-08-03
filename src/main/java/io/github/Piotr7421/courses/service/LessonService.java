package io.github.Piotr7421.courses.service;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public List<LessonDto> findAll() {
        return lessonRepository.findAll().stream()
                .map(LessonMapper::mapToDto)
                .toList();
    }

    public LessonDto findById(int id) {
        return lessonRepository.findById(id)
                .map(LessonMapper::mapToDto)
                .orElseThrow(() -> new LessonNotFoundException(MessageFormat
                        .format("Lesson with id={0} not found", id)));
    }

    @Transactional
    public LessonDto save(CreateLessonCommand command) {
        int teacherId = command.getTeacherId();
        int studentId = command.getStudentId();
        Lesson lesson = LessonMapper.mapFromCommand(command);
        Teacher teacher;
        try {
            teacher = teacherRepository.findWithPessimisticLockingById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                            .format("Teacher with id={0} not found", teacherId)));
        } catch (PessimisticLockingFailureException e) {
            throw new TeacherLockTimeoutException("Could not acquire lock on teacher - operation timed out");
        }
        LocalDateTime dateMinusHour = lesson.getDate().minusHours(1);
        LocalDateTime datePlusHour = lesson.getDate().plusHours(1);
        if (lessonRepository.existsByTeacherAndDateAfterAndDateBefore(teacher, dateMinusHour, datePlusHour)) {
            throw new OverlappingLessonException("Lesson overlaps with another lesson for the same teacher.");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(MessageFormat
                        .format("Student with id={0} not found", studentId)));
        lesson.setTeacher(teacher);
        lesson.setStudent(student);
        return LessonMapper.mapToDto(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonDto update(int id, UpdateLessonCommand command) {
        Lesson existingLesson = lessonRepository.findWithLockingById(id)
                .orElseThrow(() -> new LessonNotFoundException(MessageFormat
                        .format("Lesson with id={0} not found", id)));
        int teacherId = existingLesson.getTeacher().getId();
        Teacher teacher;
        try {
            teacher = teacherRepository.findWithPessimisticLockingById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                            .format("Teacher with id={0} not found", teacherId)));
        } catch (PessimisticLockingFailureException e) {
            throw new TeacherLockTimeoutException("Could not acquire lock on teacher - operation timed out");
        }
        LocalDateTime newDate = command.getDate();
        LocalDateTime dateMinusHour = newDate.minusHours(1);
        LocalDateTime datePlusHour = newDate.plusHours(1);
        if (lessonRepository.existsByTeacherAndDateAfterAndDateBefore(teacher, dateMinusHour, datePlusHour)) {
            throw new OverlappingLessonException("Lesson overlaps with another lesson for the same teacher.");
        }
        existingLesson.setDate(newDate);
        return LessonMapper.mapToDto(lessonRepository.saveAndFlush(existingLesson));
    }

    @Transactional
    public void deleteById(int id) {
        if (lessonRepository.existsByIdAndDateBefore(id, LocalDateTime.now())) {
            throw new LessonAlreadyStartedException("Deletion of a started lesson is forbidden");
        }
        lessonRepository.deleteById(id);
    }
}
