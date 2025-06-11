package pl.spring.courses.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import pl.spring.courses.common.Language;
import pl.spring.courses.exception.TeacherLockTimeoutException;
import pl.spring.courses.model.Student;
import pl.spring.courses.model.Teacher;
import pl.spring.courses.model.command.CreateStudentCommand;
import pl.spring.courses.repository.StudentRepository;
import pl.spring.courses.repository.TeacherRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test-lock")
public class StudentServicePessimisticLockTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String DB_NAME = "courses_test_lock";

    private int teacherId1;
    private CreateStudentCommand command1;
    private CreateStudentCommand command2;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeAll
    public static void setupDatabase() {
        try (Connection connection = createConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DROP DATABASE IF EXISTS " + DB_NAME);
            statement.executeUpdate("CREATE DATABASE " + DB_NAME);

            System.out.println("Test database '" + DB_NAME + "' created successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    @AfterAll
    public static void cleanupDatabase() {
        try (Connection connection = createConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DROP DATABASE IF EXISTS " + DB_NAME);

            System.out.println("Test database '" + DB_NAME + "' dropped successfully");
        } catch (SQLException e) {
            System.err.println("Warning: Failed to clean up test database: " + e.getMessage());
        }
    }

    private static Connection createConnection() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        return dataSource.getConnection();
    }

    @BeforeEach
    public void setUp() {
        Teacher teacher1 = Teacher.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .languages(Set.of(Language.JAVA, Language.PYTHON))
                .active(true)
                .build();
        Teacher savedTeacher1 = teacherRepository.save(teacher1);
        teacherId1 = savedTeacher1.getId();

        command1 = CreateStudentCommand.builder()
                .firstName("Ula")
                .lastName("Panek")
                .language(Language.JAVA)
                .teacherId(teacherId1)
                .build();

        command2 = CreateStudentCommand.builder()
                .firstName("Stefek")
                .lastName("Burczymucha")
                .language(Language.JAVA)
                .teacherId(teacherId1)
                .build();
    }

    @AfterEach
    public void cleanUp() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    @Test
    void create_WhenTeachersLockTimeIsLessThanLockTimeout_ShouldCreateNewStudent() throws Exception {
        long studentsBefore = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsBefore);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        CountDownLatch createLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        executor.submit(() -> {
            transactionTemplate.execute(status -> {
                teacherRepository.findWithPessimisticLockingById(teacherId1);
                createLatch.countDown();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finishLatch.countDown();
                return null;
            });
        });

        createLatch.await();
        Thread.sleep(100);
        studentService.create(command1);
        finishLatch.countDown();

        assertTrue(finishLatch.await(3, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(4, TimeUnit.SECONDS));

        long studentsAfter = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(1, studentsAfter);
    }

    @Test
    void create_WhenTeachersLockTimeIsLongerThanLockTimeout_ShouldThrowTeacherLockTimeoutException() throws Exception {
        long studentsBefore = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsBefore);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        CountDownLatch createLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        executor.submit(() -> {
            transactionTemplate.execute(status -> {

                teacherRepository.findWithPessimisticLockingById(teacherId1);
                createLatch.countDown();
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finishLatch.countDown();
                return null;
            });
        });

        createLatch.await();
        Thread.sleep(100);
        assertThatExceptionOfType(TeacherLockTimeoutException.class)
                .isThrownBy(() -> studentService.create(command1))
                .withMessage("Could not acquire lock on teacher - operation timed out");
        finishLatch.countDown();

        assertTrue(finishLatch.await(3, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(4, TimeUnit.SECONDS));

        long studentsAfter = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsAfter);
    }

    @Test
    public void create_WithNoTeachersLock_ShouldSuccessfulCreateStudent() {

        long studentsBefore = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsBefore);

        var result = studentService.create(command1);

        assertNotNull(result);
        assertEquals(command1.getFirstName(), result.getFirstName());
        assertEquals(command1.getLastName(), result.getLastName());
        assertEquals(command1.getLanguage(), result.getLanguage());
        assertEquals(command1.getTeacherId(), result.getTeacherId());

        long studentsAfter = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(1, studentsAfter);
    }

    @Test
    public void create_WhenTwoConcurrentStudentsWithTheSameTeacher_ShouldCreateStudents() throws InterruptedException {
        long studentsBefore = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsBefore);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            try {
                startLatch.await();
                studentService.create(command1);
                finishLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executorService.submit(() -> {
            try {
                startLatch.await();
                studentService.create(command2);
                finishLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        startLatch.countDown();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long studentsAfter = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(2, studentsAfter);
    }

    @Test
    public void create_WhenOneHundredConcurrentStudentsWithTheSameTeacher_ShouldCreateStudents() throws InterruptedException {
        long studentsBefore = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(0, studentsBefore);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    studentService.create(command1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        startLatch.countDown();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long studentsAfter = studentRepository.findAll().stream().filter(Student::isActive).toList().size();
        assertEquals(100, studentsAfter);
    }
}
