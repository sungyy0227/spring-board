package spring.board.comment.dto;

import spring.board.comment.domain.Comment;

public record CommentResponse(
        Long id,
        String commenter,
        String commentContent,
        Long memberId,
        boolean memberWithdrawn
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getCommenter(),
                comment.getCommentContent(),
                comment.getMember() == null ? null : comment.getMember().getId(),
                comment.getMember() != null && comment.getMember().isWithdrawn()
        );
    }
}
