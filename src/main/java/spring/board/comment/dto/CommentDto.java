package spring.board.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentDto {
    private Long postId;

    //작성자 검증은 서비스에서 함
    private String commenter;

    private String commentContent;

    private String guestRawPassword;

    public static CommentDto from(CommentCreateRequest request) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommenter(request.commenter());
        commentDto.setCommentContent(request.content());
        commentDto.setGuestRawPassword(request.guestPassword());
        return commentDto;
    }

    public String getGuestRawPassword() {
        return guestRawPassword;
    }

    public void setGuestRawPassword(String guestRawPassword) {
        this.guestRawPassword = guestRawPassword;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getCommenter() {
        return commenter;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }
}
