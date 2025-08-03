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
import io.github.Piotr7421.courses.model.command.CreateLessonCommand;
import io.github.Piotr7421.courses.model.command.UpdateLessonCommand;
import io.github.Piotr7421.courses.model.dto.LessonDto;
import io.github.Piotr7421.courses.service.LessonService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public List<LessonDto> findAll() {
        return (lessonService.findAll());
    }

    @GetMapping("/{id}")
    public LessonDto findById(@PathVariable int id) {
        return lessonService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto create(@RequestBody @Valid CreateLessonCommand command) {
        return lessonService.save(command);
    }

    @PatchMapping("/{id}")
    public LessonDto update(@PathVariable int id, @RequestBody @Valid UpdateLessonCommand command) {
        return lessonService.update(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        lessonService.deleteById(id);
    }
}
