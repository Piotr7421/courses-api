package pl.spring.lessons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.spring.lessons.model.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

}
