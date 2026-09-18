package spring.board.post.dto;

import spring.board.comment.domain.Comment;
import spring.board.post.domain.Post;
import spring.board.comment.dto.CommentResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String poster,
        int viewCount,
        LocalDateTime createdAt,
        Long memberId,
        boolean memberWithdrawn,
        List<CommentResponse> comments
) {
    public static PostDetailResponse from(Post post) {
        List<CommentResponse> comments = new ArrayList<>();
        for (Comment comment : post.getComments()) {
            comments.add(CommentResponse.from(comment));
        }
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPoster(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getMember() == null ? null : post.getMember().getId(),
                post.getMember() != null && post.getMember().isWithdrawn(),
                comments
        );
    }
}
