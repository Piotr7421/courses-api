package pl.spring.courses.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.spring.courses.model.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    @Lock(LockModeType.OPTIMISTIC)
    @EntityGraph(attributePaths = "languages")
    Optional<Teacher> findWithLockingById(int teacherId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "languages")
    Optional<Teacher> findWithPessimisticLockingById(int teacherId);

    @EntityGraph(attributePaths = "languages")
    List<Teacher> findAll();

    @EntityGraph(attributePaths = "languages")
    Optional<Teacher> findById(int id);

    @EntityGraph(attributePaths = {"languages", "students"})
    @Query("select t from Teacher t where t.id = :id")
    Optional<Teacher> findByIdWithStudents(@Param("id") int id);
}
