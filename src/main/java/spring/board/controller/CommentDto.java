package spring.board.controller;

public class CommentDto {
    private Long postId;
    private String commenter;
    private String commentContent;
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
