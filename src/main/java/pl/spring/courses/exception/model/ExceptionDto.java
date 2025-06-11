package pl.spring.courses.exception.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ExceptionDto {

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String message;
}
