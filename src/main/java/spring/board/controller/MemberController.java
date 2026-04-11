package spring.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.dto.LoginDto;
import spring.board.dto.MemberDto;
import spring.board.dto.SessionMember;
import spring.board.repository.MemberRepository;
import spring.board.service.CommentService;
import spring.board.service.MemberService;
import spring.board.service.PostService;

import java.util.List;

@Controller
public class MemberController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(PostService postService, CommentService commentService, MemberService memberService, MemberRepository memberRepository) {
        this.postService = postService;
        this.commentService=commentService;
        this.memberService=memberService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/login")
    public String loginPage(Model model){
        model.addAttribute("loginDto",new LoginDto());
        return "login";
    }

    @GetMapping("/login/signup")
    public String signupPage(Model model){
        model.addAttribute("memberdto",new MemberDto());
        return "signup";
    }

    @PostMapping("/login/signup")
    public String signup(MemberDto memberDto,RedirectAttributes redirectAttributes){
        try{
            memberService.signup(memberDto);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다.");
            return "redirect:/login";
        }
        catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login/signup";
        }
    }

    @PostMapping("/login")
    public String login(LoginDto loginDto, HttpServletRequest request, RedirectAttributes redirectAttributes){
        try{
            Member member= memberService.login(loginDto.getLoginId(),loginDto.getPassword());

            //세션 생성, 없으면 만들고 있으면 기존꺼 사용
            HttpSession session=request.getSession(true);
            SessionMember sessionMember=new SessionMember(member.getId(), member.getLoginId(), member.getNickname(), member.getRole());
            session.setAttribute("loginMember",sessionMember);

            return "redirect:/";
        }catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session=request.getSession(false);
        if(session!= null){
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    @GetMapping("/admin/members") //중복된 유저나 없는 유저 예외 처리 하기
    public String searchMember(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String mode,
            Model model
    ) {
        try{
            Member member=memberService.findMember(keyword,mode);
            model.addAttribute("member",member);
        }
        catch (IllegalArgumentException e){
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "admin";
    }

    @PostMapping("/admin/members/{id}/grant-admin")
    public String grandAdmin(@PathVariable Long id){
        memberService.grantAdmin(id);
        return "redirect:/admin/members/{id}";
    }

    @PostMapping("/admin/members/{id}/remove-admin")
    public String removeAdmin(@PathVariable Long id){
        memberService.removeAdmin(id);
        return "redirect:/admin/members/{id}";
    }

    @GetMapping("/admin/members/{id}")
    public String memberDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes){
        try {
            Member member = memberService.findById(id);
            List<Post> posts = postService.findPostsByMemberId(id);
            List<Comment> comments = commentService.findCommentsByMemberId(id);

            model.addAttribute("member", member);
            model.addAttribute("posts", posts);
            model.addAttribute("comments", comments);

            return "memberDetail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin";
        }
    }


}
