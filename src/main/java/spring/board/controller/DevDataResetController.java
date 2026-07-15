package spring.board.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.service.MemberService;
import spring.board.service.PostService;

@Controller
@Profile("dev")
public class DevDataResetController {
    private final PostService postService;
    private final MemberService memberService;

    public DevDataResetController(PostService postService, MemberService memberService) {
        this.postService = postService;
        this.memberService = memberService;
    }

    @PostMapping("/dev/reset/posts")
    public String delAllPost(RedirectAttributes redirectAttributes) {
        postService.deleteAllAndResetId();
        redirectAttributes.addFlashAttribute("resetSuccessMessage", "개발용 게시글과 댓글을 초기화했습니다.");
        return "redirect:/admin";
    }

    @PostMapping("/dev/reset/members")
    public String delAllMember(RedirectAttributes redirectAttributes) {
        memberService.resetAllMember();
        redirectAttributes.addFlashAttribute("resetSuccessMessage", "개발용 회원 데이터를 초기화했습니다.");
        return "redirect:/admin";
    }
}
