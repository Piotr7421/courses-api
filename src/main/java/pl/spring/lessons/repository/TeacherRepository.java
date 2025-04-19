package pl.spring.lessons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.spring.lessons.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
}
