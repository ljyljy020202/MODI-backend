package kuit.modi.dto.reminder;

import java.time.Instant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record ReminderQueryParams(
        @NotBlank
        String address,

        Instant since,
        Instant until,

        @Min(1)
        @Max(100)
        Integer limit,

        String cursor
) {}

