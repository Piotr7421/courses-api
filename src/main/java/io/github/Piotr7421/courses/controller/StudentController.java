package io.github.Piotr7421.courses.controller;

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
import io.github.Piotr7421.courses.model.command.CreateStudentCommand;
import io.github.Piotr7421.courses.model.command.UpdateStudentCommand;
import io.github.Piotr7421.courses.model.dto.StudentDto;
import io.github.Piotr7421.courses.service.StudentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping()
    public List<StudentDto> findAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public StudentDto findById(@PathVariable int id) {
        return studentService.findById(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDto create(@RequestBody @Valid CreateStudentCommand command) {
        return studentService.create(command);
    }

    @PatchMapping("/{id}")
    public StudentDto update(@PathVariable int id, @RequestBody @Valid UpdateStudentCommand command) {
        return studentService.update(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        studentService.deleteById(id);
    }
}
