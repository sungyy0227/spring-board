package spring.board.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import spring.board.post.controller.PostApiController;

@RestControllerAdvice(assignableTypes = PostApiController.class) //PostApiController로 범위 제한
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception){
        ApiErrorResponse response = ApiErrorResponse.from(exception.getErrorCode());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException exception){
        ApiErrorResponse response = ApiErrorResponse.from(exception.getErrorCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException exception){
        ApiErrorResponse response = ApiErrorResponse.from(exception.getErrorCode());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

}
