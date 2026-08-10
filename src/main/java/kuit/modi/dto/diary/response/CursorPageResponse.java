package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private String nextCursor;
    private boolean hasNext;
}
