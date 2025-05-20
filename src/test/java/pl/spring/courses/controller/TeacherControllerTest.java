package pl.spring.courses.controller;

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
import pl.spring.courses.common.Language;
import pl.spring.courses.model.Teacher;
import pl.spring.courses.model.command.CreateTeacherCommand;
import pl.spring.courses.repository.TeacherRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    }

    @AfterEach
    public void tearDown() {
        teacherRepository.deleteAll();
    }

    @Test
    void findAll_ShouldReturnListOfTeachers() throws Exception {
        mockMvc.perform(get("/api/v1/teachers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(teacher.getId()))
                .andExpect(jsonPath("$[0].firstName").value("Anna"))
                .andExpect(jsonPath("$[0].lastName").value("Nowak"))
                .andExpect(jsonPath("$[0].languages", containsInAnyOrder("JAVA", "PYTHON")))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void findAll_WhenNoTeachers_ShouldReturnEmptyList() throws Exception {
        teacherRepository.deleteAll(); // Czyszczenie przed testem

        mockMvc.perform(get("/api/v1/teachers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_ShouldReturnTeacherDto() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/" + teacher.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teacher.getId()))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Nowak"))
                .andExpect(jsonPath("$.languages", containsInAnyOrder("JAVA", "PYTHON")))
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void findById_WhenTeacherNotFound_ShouldThrowException() throws Exception {
        int teacherId = 10;
        mockMvc.perform(get("/api/v1/teachers/" + teacherId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher with id=10 not found"));
    }

    @Test
    void findById_WhenInvalidIdFormat_ShouldReturnBadRequestFor() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/abc"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenValidData_ShouldReturnCreatedTeacher() throws Exception {
        CreateTeacherCommand command = CreateTeacherCommand.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .languages(Set.of(Language.JAVA, Language.PYTHON))
                .build();

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.languages", containsInAnyOrder("JAVA", "PYTHON")));

        Teacher savedTeacher = teacherRepository.findAll().get(1);
        assertThat(savedTeacher.getFirstName()).isEqualTo("Jan");
        assertThat(savedTeacher.getLastName()).isEqualTo("Kowalski");
        assertThat(savedTeacher.getLanguages()).containsExactlyInAnyOrder(Language.JAVA, Language.PYTHON);
    }

    @Test
    void create_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        CreateTeacherCommand invalidCommand = CreateTeacherCommand.builder()
                .firstName("")
                .lastName("Kowalski")
                .languages(Set.of())
                .build();

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommand)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.violations").value(hasSize(2)))
                .andExpect(jsonPath("$.violations").value(hasItem(
                        allOf(
                                hasEntry("field", "firstName"),
                                hasEntry("message", "PATTERN_MISMATCH_[A-Z][a-z]{1,19}")
                        ))))
                .andExpect(jsonPath("$.violations").value(hasItem(
                        allOf(
                                hasEntry("field", "languages"),
                                hasEntry("message", "EMPTY_VALUE")
                        ))));
    }
}

