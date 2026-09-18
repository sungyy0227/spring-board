package spring.board.admin.dto;

import spring.board.post.service.PostService.ResetResult;

public record BoardResetResponse(
        long imageCount,
        long commentCount,
        long postCount,
        String message
) {
    public static BoardResetResponse from(ResetResult result) {
        return new BoardResetResponse(
                result.imageCount(),
                result.commentCount(),
                result.postCount(),
                "이미지 " + result.imageCount() + "개, 댓글 " + result.commentCount()
                        + "개, 게시글 " + result.postCount() + "개를 삭제했습니다."
        );
    }
}
