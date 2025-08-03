package io.github.Piotr7421.courses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import io.github.Piotr7421.courses.common.Language;
import io.github.Piotr7421.courses.model.Student;
import io.github.Piotr7421.courses.model.Teacher;
import io.github.Piotr7421.courses.model.command.CreateStudentCommand;
import io.github.Piotr7421.courses.model.command.UpdateStudentCommand;
import io.github.Piotr7421.courses.repository.StudentRepository;
import io.github.Piotr7421.courses.repository.TeacherRepository;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Student student;
    private Teacher teacher;

    @BeforeEach
    public void setUp() {

         teacher = Teacher.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .languages(Set.of(Language.JAVA, Language.PYTHON))
                 .active(true)
                .build();
        teacherRepository.save(teacher);

         student = Student.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .language(Language.JAVA)
                .teacher(teacher)
                 .active(true)
                .build();
        studentRepository.save(student);
    }

    @AfterEach
    public void tearDown() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    @Test
    void findAll_ShouldReturnListOfStudents() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(student.getId()))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[0].lastName").value("Kowalski"))
                .andExpect(jsonPath("$[0].language").value("JAVA"))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void findById_ShouldReturnStudentDto() throws Exception {
        mockMvc.perform(get("/api/v1/students/" + student.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void findById_WhenStudentNotFound_ShouldThrowException() throws Exception {
        int studentId = 10;
        mockMvc.perform(get("/api/v1/students/" + studentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student with id=10 not found"));
    }

    @Test
    void create_ShouldCreateStudent() throws Exception {
        CreateStudentCommand command = CreateStudentCommand.builder()
                .firstName("Kaziu")
                .lastName("Kafka")
                .language(Language.PYTHON)
                .teacherId(teacher.getId())
                .build();
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Kaziu"))
                .andExpect(jsonPath("$.lastName").value("Kafka"))
                .andExpect(jsonPath("$.language").value("PYTHON"))
                .andExpect(jsonPath("$.teacherId").value(teacher.getId()))
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void create_WhenTeacherNotFound_ShouldThrowException() throws Exception {
        CreateStudentCommand command = CreateStudentCommand.builder()
                .firstName("Kaziu")
                .lastName("Kafka")
                .language(Language.PYTHON)
                .teacherId(10)
                .build();
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher with id=10 not found"));
    }

    @Test
    void create_WhenTeacherLanguageIsIncompatible_ShouldThrowIncompatibleTeacherLanguageException() throws Exception {
        CreateStudentCommand command = CreateStudentCommand.builder()
                .firstName("Kaziu")
                .lastName("Kafka")
                .language(Language.COBOL)
                .teacherId(teacher.getId())
                .build();
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incompatible teacher language, teacher id=" + teacher.getId() + ", language=COBOL"));
    }

    @Test
    void update_ShouldUpdateStudent() throws Exception {
        Teacher newTeacher = Teacher.builder()
                .firstName("Zuza")
                .lastName("Pawlak")
                .languages(Set.of(Language.JAVA, Language.COBOL))
                .active(true)
                .build();
        Teacher saved = teacherRepository.save(newTeacher);

        UpdateStudentCommand command = UpdateStudentCommand.builder()
                .teacherId(saved.getId())
                .build();

        mockMvc.perform(patch("/api/v1/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.teacherId").value(saved.getId()))
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void update_WhenStudentNotFound_ShouldThrowException() throws Exception {
        int studentId = 10;
        UpdateStudentCommand command = UpdateStudentCommand.builder()
                .teacherId(teacher.getId())
                .build();

        mockMvc.perform(patch("/api/v1/students/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student with id=10 not found"));
    }

    @Test
    void update_WhenTeacherNotFound_ShouldThrowException() throws Exception {
        UpdateStudentCommand command = UpdateStudentCommand.builder()
                .teacherId(10)
                .build();

        mockMvc.perform(patch("/api/v1/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher with id=10 not found"));
    }

    @Test
    void update_WhenTeacherLanguageIsIncompatible_ShouldThrowIncompatibleTeacherLanguageException() throws Exception {
        Teacher newTeacher = Teacher.builder()
                .firstName("Zuza")
                .lastName("Pawlak")
                .languages(Set.of(Language.C, Language.COBOL))
                .active(true)
                .build();
        teacherRepository.save(newTeacher);

        UpdateStudentCommand command = UpdateStudentCommand.builder()
                .teacherId(newTeacher.getId())
                .build();

        mockMvc.perform(patch("/api/v1/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incompatible teacher language, teacher id=" + newTeacher.getId() + ", language=JAVA"));
    }

    @Test
    void delete_ShouldDeleteStudent() throws Exception {
        mockMvc.perform(get("/api/v1/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/v1/students/" + student.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}