package spring.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.dto.SignupForm;
import spring.board.dto.SignupValidationError;
import spring.board.security.CustomUserDetails;
import spring.board.service.CommentService;
import spring.board.service.MemberService;
import spring.board.service.PostService;

import java.util.List;

@Controller
public class MemberController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;

    @Autowired
    public MemberController(PostService postService, CommentService commentService, MemberService memberService) {
        this.postService = postService;
        this.commentService = commentService;
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/login/signup")
    public String signupPage(Model model){
        model.addAttribute("signupForm",new SignupForm());
        return "signup";
    }

    @PostMapping("/login/signup")
    public String signup(SignupForm signupForm, RedirectAttributes redirectAttributes, Model model){
        List<SignupValidationError> validationErrors = memberService.signup(signupForm);
        if(validationErrors.isEmpty()){ //회원가입 검증 성공
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다.");
            return "redirect:/login";
        }
        model.addAttribute("validationErrors", validationErrors);
        model.addAttribute("signupForm", signupForm);
        return "signup";

    }

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal CustomUserDetails loginMember){
        if (loginMember==null){
            return "redirect:/";
        }
        try {
            Member member = memberService.findById(loginMember.getId());
            List<Post> posts = postService.findPostsByMemberId(loginMember.getId());
            List<Comment> comments = commentService.findCommentsByMemberId(loginMember.getId());

            model.addAttribute("member", member);
            model.addAttribute("posts", posts);
            model.addAttribute("comments", comments);

        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }

        return "mypage";
    }

    @GetMapping("/mypage/withdraw") //mypage는 시큐리티로 url을 막을거여서 따로 인가조치가 필요없음
    public String withdrawPageRequest(){
        return "withdraw";
    }

    @PostMapping("/mypage/withdraw")
    public String withdrawMember(@RequestParam String password, HttpServletRequest request, RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails loginMember, HttpServletResponse response) {

        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            memberService.withdraw(password, loginMember.getId());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage/withdraw";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication!=null){
            new SecurityContextLogoutHandler().logout(request,response,authentication);
        }
        return "redirect:/";
    }
}
