package spring.board.post.dto;

import spring.board.post.domain.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String poster,
        int viewCount,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getPoster(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
