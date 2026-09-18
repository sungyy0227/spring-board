package spring.board.comment.dto;

public record CommentCreateRequest(
        String commenter,
        String content,
        String guestPassword
) {
}
