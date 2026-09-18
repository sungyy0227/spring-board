package spring.board.post.dto;

public record PostCreateResponse(
        Long postId
) {
    public static PostCreateResponse from(Long postId) {
        return new PostCreateResponse(postId);
    }
}
