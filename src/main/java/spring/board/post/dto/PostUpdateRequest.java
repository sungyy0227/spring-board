package spring.board.post.dto;

import java.util.List;

public record PostUpdateRequest(
        String title,
        String content,
        String poster,
        List<Long> imageIds
) {
}
