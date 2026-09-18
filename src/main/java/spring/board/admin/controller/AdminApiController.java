package spring.board.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.board.admin.dto.AdminRuntimeResponse;
import spring.board.admin.dto.BoardResetRequest;
import spring.board.admin.dto.BoardResetResponse;
import spring.board.comment.domain.Comment;
import spring.board.comment.service.CommentService;
import spring.board.exception.ErrorCode;
import spring.board.exception.InvalidRequestException;
import spring.board.member.domain.Member;
import spring.board.member.dto.MemberActivityResponse;
import spring.board.member.dto.MemberResponse;
import spring.board.member.service.MemberService;
import spring.board.post.domain.Post;
import spring.board.post.service.PostService;
import spring.board.post.service.PostService.ResetResult;
import spring.board.security.service.UserSessionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {
    private static final Logger log = LoggerFactory.getLogger(AdminApiController.class);
    private static final String RESET_CONFIRMATION = "RESET BOARD";

    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final UserSessionService userSessionService;
    private final Environment environment;

    public AdminApiController(PostService postService, CommentService commentService,
                              MemberService memberService, UserSessionService userSessionService,
                              Environment environment) {
        this.postService = postService;
        this.commentService = commentService;
        this.memberService = memberService;
        this.userSessionService = userSessionService;
        this.environment = environment;
    }

    @GetMapping("/runtime")
    public AdminRuntimeResponse getRuntime() {
        return new AdminRuntimeResponse(environment.matchesProfiles("dev"));
    }

    @GetMapping("/members")
    public MemberResponse searchMember(@RequestParam String keyword, @RequestParam String mode) {
        return MemberResponse.from(memberService.findMemberByKeyword(keyword, mode));
    }

    @GetMapping("/members/{memberId}")
    public MemberActivityResponse getMember(@PathVariable Long memberId) {
        Member member = memberService.findById(memberId);
        List<Post> posts = postService.findPostsByMemberId(memberId);
        List<Comment> comments = commentService.findCommentsByMemberId(memberId);
        return MemberActivityResponse.from(member, posts, comments);
    }

    @PostMapping("/members/{memberId}/grant-admin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantAdmin(@PathVariable Long memberId) {
        memberService.grantAdmin(memberId);
        userSessionService.expireUserSessions(memberId);
    }

    @PostMapping("/members/{memberId}/remove-admin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAdmin(@PathVariable Long memberId) {
        memberService.removeAdmin(memberId);
        userSessionService.expireUserSessions(memberId);
    }

    @PostMapping("/reset/board")
    public BoardResetResponse resetBoard(@RequestBody BoardResetRequest request,
                                         Authentication authentication) {
        if (!RESET_CONFIRMATION.equals(request.confirmation())) {
            throw new InvalidRequestException(ErrorCode.ADMIN_RESET_CONFIRMATION_MISMATCH);
        }

        ResetResult result = postService.resetBoardData();
        log.warn("관리자가 게시판 데이터를 초기화했습니다. admin={}, images={}, comments={}, posts={}",
                authentication.getName(), result.imageCount(), result.commentCount(), result.postCount());
        return BoardResetResponse.from(result);
    }
}
