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

    LOGIN_FAILED("아이디 또는 비밀번호가 올바르지 않습니다."),
    WITHDRAWN_MEMBER("탈퇴한 회원입니다. 로그인이 불가능합니다."),
    AUTHENTICATION_REQUIRED("로그인이 필요합니다."),
    ACCESS_DENIED("접근 권한이 없습니다."),

    ADMIN_RESET_CONFIRMATION_MISMATCH("확인 문구가 일치하지 않습니다."),

    IMAGE_STORAGE_FAILURE("이미지 파일 처리에 실패했습니다."),

    CHAT_ROOM_NOT_FOUND("채팅방이 존재하지 않습니다."),
    CHAT_MEMBER_NOT_FOUND("채팅방 멤버가 존재하지 않습니다."),
    CHAT_MEMBER_ALREADY_EXISTS("이미 참여 중인 채팅방입니다."),
    CHAT_INVITE_NOT_FOUND("초대 링크가 존재하지 않습니다."),
    CHAT_INVITE_ALREADY_EXISTS("이미 초대 링크가 존재합니다."),
    CHAT_MESSAGE_REQUIRED("메시지를 입력해주세요."),
    CHAT_ROOM_ACCESS_DENIED("채팅방에 대한 접근 권한이 없습니다."),
    CHAT_MESSAGE_TOO_LONG("메시지는 500자 이내로 입력해주세요.");

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
