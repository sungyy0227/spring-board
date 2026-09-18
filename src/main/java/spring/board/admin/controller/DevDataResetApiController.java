package spring.board.admin.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.board.admin.dto.DevResetResponse;
import spring.board.member.service.MemberService;
import spring.board.post.service.PostService;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/admin/dev/reset")
public class DevDataResetApiController {
    private final PostService postService;
    private final MemberService memberService;

    public DevDataResetApiController(PostService postService, MemberService memberService) {
        this.postService = postService;
        this.memberService = memberService;
    }

    @PostMapping("/posts")
    public DevResetResponse resetPosts() {
        postService.deleteAllAndResetId();
        return new DevResetResponse("개발용 게시글과 댓글을 초기화했습니다.");
    }

    @PostMapping("/members")
    public DevResetResponse resetMembers() {
        memberService.resetAllMember();
        return new DevResetResponse("개발용 회원 데이터를 초기화했습니다.");
    }
}
