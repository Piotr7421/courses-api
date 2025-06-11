package pl.spring.courses.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.spring.courses.exception.DatabaseConstraintException;
import pl.spring.courses.exception.StudentNotFoundException;
import pl.spring.courses.exception.StudentOptimisticLockException;
import pl.spring.courses.exception.TeacherLockTimeoutException;
import pl.spring.courses.exception.TeacherNotFoundException;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherLanguageValidator teacherLanguageValidator;

    public List<StudentDto> findAll() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::mapToDto)
                .toList();
    }

    public StudentDto findById(int id) {
        return studentRepository.findById(id)
                .map(StudentMapper::mapToDto)
                .orElseThrow(() -> new StudentNotFoundException(MessageFormat
                        .format("Student with id={0} not found", id)));
    }

    @Transactional
    public StudentDto create(CreateStudentCommand command) {
        int teacherId = command.getTeacherId();
        Teacher teacher;
        try {
            teacher = teacherRepository.findWithPessimisticLockingById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException((MessageFormat
                            .format("Teacher with id={0} not found", teacherId))));
        } catch (PessimisticLockingFailureException e) {
            throw new TeacherLockTimeoutException("Could not acquire lock on teacher - operation timed out");
        }
        Student student = StudentMapper.mapFromCommand(command);
        teacherLanguageValidator.validateTeacherLanguage(teacher, student);
        student.setTeacher(teacher);
        try {
            return StudentMapper.mapToDto(studentRepository.saveAndFlush(student));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseConstraintException("Violation of integrity constraints while student insertion to the database");
        }
    }

    @Transactional
    public StudentDto update(int id, UpdateStudentCommand command) {
        int teacherId = command.getTeacherId();
        Teacher teacher;
        try {
            teacher = teacherRepository.findWithPessimisticLockingById(teacherId)
                    .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                            .format("Teacher with id={0} not found", teacherId)));
        } catch (PessimisticLockingFailureException e) {
            throw new TeacherLockTimeoutException("Could not acquire lock on teacher - operation timed out");
        }
        Student student = studentRepository.findWithLockingById(id)
                .orElseThrow(() -> new StudentNotFoundException(MessageFormat
                        .format("Student with id={0} not found", id)));
        student.setTeacher(teacher);
        teacherLanguageValidator.validateTeacherLanguage(teacher, student);
        try {
            return StudentMapper.mapToDto(studentRepository.saveAndFlush(student));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseConstraintException("Violation of integrity constraints while student update to the database");
        } catch (OptimisticLockingFailureException e) {
            throw new StudentOptimisticLockException("Student was modified by another transaction");
        }
    }

    @Transactional
    public void deleteById(int id) {
        studentRepository.deleteById(id);
    }
}























