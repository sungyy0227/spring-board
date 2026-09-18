package spring.board.member.dto;

import java.util.List;

public record SignupErrorResponse(
        String code,
        String message,
        List<SignupValidationError> validationErrors
) {
    public static SignupErrorResponse from(List<SignupValidationError> validationErrors) {
        return new SignupErrorResponse(
                "VALIDATION_FAILED",
                "입력값이 올바르지 않습니다.",
                validationErrors
        );
    }
}
