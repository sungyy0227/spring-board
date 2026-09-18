package spring.board.post.dto;

import java.util.List;

public record PostCreateRequest(
        String title,
        String content,
        String poster,
        String guestPassword,
        List<Long> imageIds
) {
}
