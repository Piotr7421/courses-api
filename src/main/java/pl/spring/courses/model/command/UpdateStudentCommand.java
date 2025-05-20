package pl.spring.courses.model.command;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UpdateStudentCommand {

    @Positive
    private int teacherId;
}
