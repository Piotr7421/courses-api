package pl.spring.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.spring.courses.model.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
}
