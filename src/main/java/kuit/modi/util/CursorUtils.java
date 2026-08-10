package kuit.modi.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CursorUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public record Decoded(LocalDateTime createdAt, Long id) {}

    public static String encode(LocalDateTime createdAt, Long id) {
        String raw = createdAt.format(FMT) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Decoded decode(String cursor) {
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|", 2);
        return new Decoded(LocalDateTime.parse(parts[0], FMT), Long.parseLong(parts[1]));
    }
}
