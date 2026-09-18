package spring.board.post.dto;

import spring.board.post.domain.Post;

public record PostEditResponse(
        Long id,
        String title,
        String content,
        String poster,
        boolean guest
) {
    public static PostEditResponse from(Post post) {
        return new PostEditResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPoster(),
                post.getMember() == null
        );
    }
}
