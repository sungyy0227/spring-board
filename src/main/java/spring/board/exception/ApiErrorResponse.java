package spring.board.exception;

public record ApiErrorResponse(
        String code,
        String message
) {
    public static ApiErrorResponse from(ErrorCode errorCode) {
        return new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
