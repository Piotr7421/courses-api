package pl.spring.courses.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.spring.courses.exception.DatabaseConstraintException;
import pl.spring.courses.exception.TeacherHasStudentsException;
import pl.spring.courses.exception.TeacherNotFoundException;
import pl.spring.courses.exception.TeacherOptimisticLockException;
import pl.spring.courses.mapper.TeacherMapper;
import pl.spring.courses.model.Teacher;
import pl.spring.courses.model.command.CreateTeacherCommand;
import pl.spring.courses.model.command.UpdateTeacherCommand;
import pl.spring.courses.model.dto.TeacherDto;
import pl.spring.courses.repository.TeacherRepository;
import pl.spring.courses.validator.TeacherLanguageValidator;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherLanguageValidator teacherLanguageValidator;

    public List<TeacherDto> findAll() {
        return teacherRepository.findAll().stream()
                .map(TeacherMapper::mapToDto)
                .toList();
    }

    public TeacherDto findById(int id) {
        return teacherRepository.findById(id)
                .map(TeacherMapper::mapToDto)
                .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                        .format("Teacher with id={0} not found", id)));
    }

    @Transactional
    public TeacherDto create(CreateTeacherCommand command) {
        Teacher toSave = TeacherMapper.mapFromCommand(command);
        try {
            return TeacherMapper.mapToDto(teacherRepository.save(toSave));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseConstraintException("Violation of integrity constraints while teacher insertion to the database");
        }
    }

    @Transactional
    public TeacherDto update(int id, UpdateTeacherCommand command) {
        Teacher teacher = teacherRepository.findWithLockingById(id)
                .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                        .format("Teacher with id={0} not found", id)));
        teacher.setLanguages(command.getLanguages());
        teacherLanguageValidator.validateTeacherLanguages(teacher);
        try {
            return TeacherMapper.mapToDto(teacherRepository.saveAndFlush(teacher));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseConstraintException("Violation of integrity constraints while teacher update to the database");
        } catch (OptimisticLockingFailureException e) {
            throw new TeacherOptimisticLockException("Teacher was modified by another transaction");
        }
    }

    @Transactional
    public void delete(int id) {
        Teacher teacher = teacherRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new TeacherNotFoundException(MessageFormat
                        .format("Teacher with id={0} not found", id)));
        if (!teacher.getStudents().isEmpty()) {
            throw new TeacherHasStudentsException(MessageFormat
                    .format("Teacher with id={0} has students.", id));
        }
        teacherRepository.deleteById(id);
    }
}


