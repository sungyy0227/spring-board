package spring.board.comment.dto;

import spring.board.comment.domain.Comment;

public record CommentResponse(
        Long id,
        String commenter,
        String commentContent
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getCommenter(),
                comment.getCommentContent()
        );
    }
}
