package pl.spring.lessons.model.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateLessonCommand {

    @NotNull(message = "NULL_VALUE")
    @Future(message = "PAST_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @Positive
    private int teacherId;

    @Positive
    private int studentId;
}
