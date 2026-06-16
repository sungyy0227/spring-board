package spring.board.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentDto {
    private Long postId;

    //작성자 검증은 서비스에서 함
    private String commenter;

    @NotBlank(message = "내용은 필수입니다.")
    private String commentContent;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String guestRawPassword;

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
