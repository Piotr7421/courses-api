package pl.spring.courses.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import pl.spring.courses.model.Lesson;
import pl.spring.courses.model.Teacher;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    boolean existsByTeacherAndDateAfterAndDateBefore(Teacher teacher, LocalDateTime dateMinusHour, LocalDateTime datePlusHour);

    boolean existsByIdAndDateBefore(int id, LocalDateTime now);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Lesson> findWithLockingById(int id);
}
