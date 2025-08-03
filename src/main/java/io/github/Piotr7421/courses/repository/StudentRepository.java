package io.github.Piotr7421.courses.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import io.github.Piotr7421.courses.model.Student;

import java.util.Optional;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Student> findWithLockingById(int id);

    Set<Student> findAllByTeacherId(int id);
}
