package spring.board.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.service.CommentService;
import spring.board.service.MemberService;
import spring.board.service.PostService;
import spring.board.service.PostService.ResetResult;
import spring.board.service.UserSessionService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final String RESET_CONFIRMATION = "RESET BOARD";

    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final UserSessionService userSessionService;
    private final Environment environment;

    public AdminController(PostService postService, CommentService commentService, MemberService memberService,
                           UserSessionService userSessionService, Environment environment) {
        this.postService = postService;
        this.commentService = commentService;
        this.memberService = memberService;
        this.userSessionService = userSessionService;
        this.environment = environment;
    }

    @GetMapping
    public String admin(Model model) {
        addAdminRuntimeModel(model);
        return "admin";
    }

    @GetMapping("/members")
    public String searchMember(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String mode,
                               Model model) {
        addAdminRuntimeModel(model);
        try {
            Member member = memberService.findMemberByKeyword(keyword, mode);
            model.addAttribute("member", member);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "admin";
    }

    @GetMapping("/members/{id}")
    public String memberDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.findById(id);
            List<Post> posts = postService.findPostsByMemberId(id);
            List<Comment> comments = commentService.findCommentsByMemberId(id);

            model.addAttribute("member", member);
            model.addAttribute("posts", posts);
            model.addAttribute("comments", comments);

            return "memberDetail";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/members/{id}/grant-admin")
    public String grantAdmin(@PathVariable Long id) {
        memberService.grantAdmin(id);
        userSessionService.expireUserSessions(id);
        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/members/{id}/remove-admin")
    public String removeAdmin(@PathVariable Long id) {
        memberService.removeAdmin(id);
        userSessionService.expireUserSessions(id);
        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/reset/board")
    public String resetBoardData(@RequestParam String confirmation, Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (!RESET_CONFIRMATION.equals(confirmation)) {
            redirectAttributes.addFlashAttribute("resetErrorMessage", "확인 문구가 일치하지 않습니다.");
            return "redirect:/admin";
        }

        try {
            ResetResult result = postService.resetBoardData();
            log.warn("관리자가 게시판 데이터를 초기화했습니다. admin={}, images={}, comments={}, posts={}",
                    authentication.getName(), result.imageCount(), result.commentCount(), result.postCount());
            redirectAttributes.addFlashAttribute("resetSuccessMessage",
                    "이미지 " + result.imageCount() + "개, 댓글 " + result.commentCount()
                            + "개, 게시글 " + result.postCount() + "개를 삭제했습니다.");
        } catch (RuntimeException exception) {
            log.error("게시판 데이터 초기화에 실패했습니다. admin={}", authentication.getName(), exception);
            redirectAttributes.addFlashAttribute("resetErrorMessage", "게시판 데이터 초기화에 실패했습니다.");
        }

        return "redirect:/admin";
    }

    private void addAdminRuntimeModel(Model model) {
        model.addAttribute("devMode", environment.matchesProfiles("dev"));
    }
}
