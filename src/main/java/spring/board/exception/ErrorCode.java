package spring.board.exception;

public enum ErrorCode {
    POST_NOT_FOUND("게시물이 존재하지 않습니다."),
    COMMENT_NOT_FOUND("댓글이 존재하지 않습니다."),
    MEMBER_NOT_FOUND("회원이 존재하지 않습니다."),

    POST_ACCESS_DENIED("게시물에 대한 권한이 없습니다."),
    COMMENT_ACCESS_DENIED("댓글에 대한 권한이 없습니다."),
    IMAGE_ACCESS_DENIED("이미지에 대한 권한이 없습니다."),

    INVALID_GUEST_PASSWORD("비밀번호가 올바르지 않습니다."),
    VALIDATION_FAILED("입력값이 올바르지 않습니다."),
    POST_TITLE_REQUIRED("제목은 필수입니다."),
    POST_CONTENT_OR_IMAGE_REQUIRED("내용 또는 이미지는 필수입니다."),
    GUEST_POSTER_REQUIRED("작성자는 필수입니다."),
    GUEST_PASSWORD_REQUIRED("비밀번호는 필수입니다."),
    SEARCH_KEYWORD_REQUIRED("검색어를 입력해주세요."),
    SEARCH_KEYWORD_TOO_SHORT("검색어를 2글자 이상 입력해주세요."),

    DUPLICATE_LOGIN_ID("이미 사용 중인 아이디입니다."),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다."),

    IMAGE_STORAGE_FAILURE("이미지 파일 처리에 실패했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
    public String getCode() {
        return name();
    }

    public String getMessage() {
        return message;
    }
}
