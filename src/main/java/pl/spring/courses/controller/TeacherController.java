package pl.spring.courses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.spring.courses.model.command.CreateTeacherCommand;
import pl.spring.courses.model.command.UpdateTeacherCommand;
import pl.spring.courses.model.dto.TeacherDto;
import pl.spring.courses.service.TeacherService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping()
    public List<TeacherDto> findAll() {
        return teacherService.findAll();
    }

    @GetMapping("/{id}")
    public TeacherDto findById(@PathVariable int id) {
        return teacherService.findById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDto create(@RequestBody @Valid CreateTeacherCommand command) {
        return teacherService.create(command);
    }

    @PatchMapping("/{id}")
    public TeacherDto update(@PathVariable int id, @RequestBody @Valid UpdateTeacherCommand command) {
        return teacherService.update(id, command);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        teacherService.delete(id);
    }
}
