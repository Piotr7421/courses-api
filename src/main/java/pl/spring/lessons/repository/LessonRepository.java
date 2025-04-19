package pl.spring.lessons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.spring.lessons.model.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Integer>{

}
