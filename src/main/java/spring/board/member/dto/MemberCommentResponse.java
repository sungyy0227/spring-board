package spring.board.member.dto;

import spring.board.comment.domain.Comment;

public record MemberCommentResponse(
        Long id,
        String content,
        Long postId,
        String postTitle
) {
    public static MemberCommentResponse from(Comment comment) {
        return new MemberCommentResponse(
                comment.getId(),
                comment.getCommentContent(),
                comment.getPost().getId(),
                comment.getPost().getTitle()
        );
    }
}
