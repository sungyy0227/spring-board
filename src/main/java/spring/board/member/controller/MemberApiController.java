package spring.board.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.board.comment.domain.Comment;
import spring.board.comment.service.CommentService;
import spring.board.member.domain.Member;
import spring.board.member.dto.MemberActivityResponse;
import spring.board.member.dto.SignupErrorResponse;
import spring.board.member.dto.SignupRequest;
import spring.board.member.dto.SignupValidationError;
import spring.board.member.dto.WithdrawRequest;
import spring.board.member.service.MemberService;
import spring.board.post.domain.Post;
import spring.board.post.service.PostService;
import spring.board.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
public class MemberApiController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;

    public MemberApiController(PostService postService, CommentService commentService,
                               MemberService memberService) {
        this.postService = postService;
        this.commentService = commentService;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        List<SignupValidationError> validationErrors = memberService.signup(request.toSignupForm());
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(SignupErrorResponse.from(validationErrors));
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public MemberActivityResponse getMyPage(
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        return getMemberActivity(loginMember.getId());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@RequestBody WithdrawRequest request,
                         @AuthenticationPrincipal CustomUserDetails loginMember,
                         HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) {
        memberService.withdraw(request.password(), loginMember.getId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(
                    httpServletRequest,
                    httpServletResponse,
                    authentication
            );
        }
    }

    private MemberActivityResponse getMemberActivity(Long memberId) {
        Member member = memberService.findById(memberId);
        List<Post> posts = postService.findPostsByMemberId(memberId);
        List<Comment> comments = commentService.findCommentsByMemberId(memberId);
        return MemberActivityResponse.from(member, posts, comments);
    }
}
