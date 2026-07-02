package com.sprint.mission.discodeit.dto.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    Object nextCursor, // int number (오프셋 페이지네이션) -> 커서 페이지네이션
    int size,
    boolean hasNext,
    Long totalElements
) {

}
