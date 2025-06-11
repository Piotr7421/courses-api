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
import pl.spring.courses.model.Lesson;
import pl.spring.courses.model.Student;
import pl.spring.courses.model.Teacher;
import pl.spring.courses.model.command.CreateLessonCommand;
import pl.spring.courses.model.command.UpdateLessonCommand;
import pl.spring.courses.repository.LessonRepository;
import pl.spring.courses.repository.StudentRepository;
import pl.spring.courses.repository.TeacherRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class LessonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    StudentRepository studentRepository;

    private Student student;
    private Teacher teacher;
    private Lesson lesson;

    @BeforeEach
    void init() {
        Language language = Language.JAVA;

        teacher = teacherRepository.save(
                Teacher.builder()
                        .firstName("Leszek")
                        .lastName("Urbański")
                        .languages(Set.of(language))
                        .active(true)
                        .build());

        student = studentRepository.save(
                Student.builder()
                        .firstName("Witek")
                        .lastName("Niedziejko")
                        .language(language)
                        .teacher(teacher)
                        .active(true)
                        .build());

        lesson = lessonRepository.save(
                Lesson.builder()
                        .date(LocalDateTime.now().plusDays(2))
                        .student(student)
                        .teacher(teacher)
                        .build());
    }

    @AfterEach
    void cleanup() {
        lessonRepository.deleteAll();
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    @Test
    void testFindAll_ResultInLessonListBeingReturned() throws Exception {
        mockMvc.perform(get("/api/v1/lessons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].teacherId").value(lesson.getTeacher().getId()))
                .andExpect(jsonPath("$[0].studentId").value(lesson.getStudent().getId()))
                .andExpect(jsonPath("$[0].date").isString())
                .andExpect(jsonPath("$[0].date").value(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(lesson.getDate())));
    }

    @Test
    void testFindAll_WhenNoLessons_ShouldReturnEmptyList() throws Exception {
        lessonRepository.deleteAll();

        mockMvc.perform(get("/api/v1/lessons"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testFindById_ResultInSpecificLessonBeingReturned() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/{id}", lesson.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.teacherId").value(lesson.getTeacher().getId()))
                .andExpect(jsonPath("$.studentId").value(lesson.getStudent().getId()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.date").value(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(lesson.getDate())));
    }

    @Test
    void testFindById_WhenLessonNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/{id}", 99))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate_HappyPath_ResultsInLessonBeingSaved() throws Exception {
        LocalDateTime lessonDate = LocalDateTime.now().plusDays(5);
        CreateLessonCommand command = CreateLessonCommand.builder()
                .date(lessonDate)
                .teacherId(teacher.getId())
                .studentId(student.getId())
                .build();

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teacherId").value(command.getTeacherId()))
                .andExpect(jsonPath("$.studentId").value(command.getStudentId()))
                .andExpect(jsonPath("$.date").isString())
                .andExpect(jsonPath("$.date").value(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(lessonDate)));
    }

    @Test
    void testCreate_WhenLessonDateIsInThePast_ResultsInLessonBeingSaved() throws Exception {
        LocalDateTime lessonDate = LocalDateTime.now().minusHours(5);
        CreateLessonCommand command = CreateLessonCommand.builder()
                .date(lessonDate)
                .teacherId(teacher.getId())
                .studentId(student.getId())
                .build();

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.violations[0].field").value("date"))
                .andExpect(jsonPath("$.violations[0].message").value("PAST_DATE"));
    }

    @Test
    void create_WhenIncorrectTeacherId_ShouldReturnNotFound() throws Exception {
        CreateLessonCommand command = CreateLessonCommand.builder()
                .date(LocalDateTime.now().plusDays(5))
                .teacherId(99)
                .studentId(student.getId())
                .build();

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void create_WhenIncorrectStudentId_ShouldReturnNotFound() throws Exception {
        CreateLessonCommand command = CreateLessonCommand.builder()
                .date(LocalDateTime.now().plusDays(5))
                .teacherId(teacher.getId())
                .studentId(99)
                .build();

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdate_HappyPath_ResultsInLessonsDateBeingUpdated() throws Exception {
        UpdateLessonCommand command = UpdateLessonCommand.builder()
                .date(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(patch("/api/v1/lessons/" + lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId").value(lesson.getTeacher().getId()))
                .andExpect(jsonPath("$.studentId").value(lesson.getStudent().getId()))
                .andExpect(jsonPath("$.date").value(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(command.getDate())));
    }

    @Test
    void testUpdate_WhenIncorrectLessonId_ShouldReturnNotFound() throws Exception {
        UpdateLessonCommand command = UpdateLessonCommand.builder()
                .date(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(patch("/api/v1/lessons/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_ResultsInLessonBeingDeleted() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/{id}", lesson.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/lessons/{id}", lesson.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/lessons/{id}", lesson.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete_WhenLessonIsAlreadyStarted_ResultsInConflict() throws Exception {
        Lesson lessonInThePast = lessonRepository.save(
                Lesson.builder()
                        .date(LocalDateTime.now().minusMinutes(5))
                        .student(student)
                        .teacher(teacher)
                        .build());

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonInThePast.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/lessons/{id}", lessonInThePast.getId()))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Deletion of a started lesson is forbidden"));

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonInThePast.getId()))
                .andExpect(status().isOk());
    }
}