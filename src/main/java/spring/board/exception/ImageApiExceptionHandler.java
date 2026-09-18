package spring.board.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import spring.board.image.controller.ImageApiController;
import spring.board.image.dto.EditorImageErrorResponse;

import java.io.IOException;

@RestControllerAdvice(assignableTypes = ImageApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImageApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EditorImageErrorResponse> handleInvalidImage(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new EditorImageErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<EditorImageErrorResponse> handleImageStorageFailure() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EditorImageErrorResponse(ErrorCode.IMAGE_STORAGE_FAILURE.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<EditorImageErrorResponse> handleUnexpectedImageFailure() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EditorImageErrorResponse("이미지 업로드 처리에 실패했습니다."));
    }
}
