package spring.board.dto;

public class SignupValidationError {
    private final String fieldName;
    private final String errorCode;
    private final String errorMessage;

    public SignupValidationError(String fieldName, String errorCode, String errorMessage) {
        this.fieldName = fieldName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
