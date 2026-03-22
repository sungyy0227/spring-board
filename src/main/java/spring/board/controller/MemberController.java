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
import spring.board.domain.Member;
import spring.board.repository.MemberRepository;
import spring.board.service.CommentService;
import spring.board.service.MemberService;
import spring.board.service.PostService;

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
    public String signup(MemberDto memberDto){
        memberService.signup(memberDto);
        return "redirect:/login";
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

//    @GetMapping("/getAdmin")
//    public String getAdmin(HttpServletRequest request){
//        HttpSession session=request.getSession(false);
//        if(session==null){
//            return "redirect:/";
//        }
//
//        SessionMember loginMember=(SessionMember) session.getAttribute("loginMember");
//        Member member=memberRepository.findByLoginId(loginMember.getLoginId()).orElseThrow();
//        member.setRole("admin");
//        memberService.join(member);
//
//        SessionMember newSessionMember =
//                new SessionMember(member.getId(), member.getLoginId(), member.getNickname(), member.getRole());
//        session.setAttribute("loginMember", newSessionMember);
//        return "redirect:/";
//    }

//    @GetMapping("/removeAdmin")
//    public String removeAdmin(HttpServletRequest request){
//        HttpSession session=request.getSession(false);
//        if(session==null){
//            return "redirect:/";
//        }
//
//        SessionMember loginMember=(SessionMember) session.getAttribute("loginMember");
//        Member member=memberRepository.findByLoginId(loginMember.getLoginId()).orElseThrow();
//        member.setRole("user");
//        memberService.join(member);
//
//        SessionMember newSessionMember =
//                new SessionMember(member.getId(), member.getLoginId(), member.getNickname(), member.getRole());
//        session.setAttribute("loginMember", newSessionMember);
//        return "redirect:/";
//    }



    @GetMapping("/admin/members") //중복된 유저나 없는 유저 예외 처리 하기
    public String searchMember(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String mode,
            Model model
    ) {

        if (keyword != null && !keyword.isBlank()) {

            Member member = null;

            if ("loginId".equals(mode)) {
                member = memberService.findByLoginId(keyword);
            } else if ("nickname".equals(mode)) {
                member = memberService.findByNickname(keyword);
            }
            model.addAttribute("member", member);
        }

        return "admin";
    }

    @PostMapping("/admin/members/{id}/grant-admin")
    public String grandAdmin(@PathVariable Long id){
        memberService.grantAdmin(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/members/{id}/remove-admin")
    public String removeAdmin(@PathVariable Long id){
        memberService.removeAdmin(id);
        return "redirect:/admin";
    }

}
