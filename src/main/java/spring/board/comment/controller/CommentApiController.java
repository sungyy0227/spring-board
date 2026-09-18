package spring.board.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.board.comment.dto.CommentCreateRequest;
import spring.board.comment.dto.CommentCreateResponse;
import spring.board.comment.dto.CommentDeleteRequest;
import spring.board.comment.dto.CommentDto;
import spring.board.comment.service.CommentService;
import spring.board.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentApiController {
    private final CommentService commentService;

    public CommentApiController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentCreateResponse createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        Long commentId = commentService.addComment(
                postId,
                CommentDto.from(request),
                loginMemberId
        );
        return new CommentCreateResponse(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody(required = false) CommentDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String guestPassword = request == null ? null : request.guestPassword();
        commentService.deleteComment(postId, commentId, loginMemberId, guestPassword);
    }
}
