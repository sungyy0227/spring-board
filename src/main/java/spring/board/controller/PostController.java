package spring.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.service.CommentService;
import spring.board.service.MemberService;
import spring.board.service.PostService;

@Controller
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PostController(PostService postService, CommentService commentService, MemberService memberService, PasswordEncoder passwordEncoder) {
        this.postService = postService;
        this.commentService=commentService;
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/")
    public String home(Model model, HttpServletRequest request){
        HttpSession session=request.getSession(false);

        if(session!=null){
            SessionMember loginMember=(SessionMember) session.getAttribute("loginMember");
            model.addAttribute("loginMember", loginMember);
        }
        
        model.addAttribute("posts", postService.getAllPost());
        return "index";
    }

    @GetMapping("/write")
    public String postWrite(Model model, HttpServletRequest request){
        HttpSession session=request.getSession(false);

        if(session!=null){
            SessionMember loginMember=(SessionMember) session.getAttribute("loginMember");
            model.addAttribute("loginMember", loginMember);
        }

        model.addAttribute("postdto",new PostDto());
        return "postWrite";
    }

    @PostMapping("/clearPost")
    public String delAllPost(){
        postService.deleteAllAndResetId();
        return "redirect:/";
    }

    @PostMapping("/clearAll")
    public String delAllPostAndMember(){
        postService.deleteAllAndResetId();
        memberService.resetAllMember();
        return "redirect:/";
    }

    @PostMapping("/uploadPost")
    public String uploadPost(PostDto postdto, HttpServletRequest request){
        SessionMember loginMember = getLoginMember(request);
        postService.uploadPost(loginMember,postdto);
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String showPost(@PathVariable Long id,Model model, HttpServletRequest request){
        Post post=postService.getPostAndIncreaseViewCount(id);
        SessionMember loginMember = getLoginMember(request);
        model.addAttribute("post",post);
        model.addAttribute("commentDto",new CommentDto());
        model.addAttribute("loginMember", loginMember);
        return "postView";
    }

    @PostMapping("/post/delete/{id}")
    public String delPost(@PathVariable Long id,
                          HttpServletRequest request,@RequestParam(required = false) String password){
        SessionMember loginMember = getLoginMember(request);
        postService.deletePost(id, password, loginMember);
        return "redirect:/";
    }

    @GetMapping("/post/modify/{id}")
    public String modifyPageRequestPost(@PathVariable Long id, Model model){
        Post post=postService.getPost(id);
        PostDto postdto=new PostDto();

        model.addAttribute("post", post);
        model.addAttribute("postdto", postdto);
        return "postModify";
    }

    @PostMapping("/post/modifyRequest/{id}")
    public String modifyRequestPost(@PathVariable Long id, PostDto postdto){
        postService.modifyPost(id,postdto);

        return "redirect:/post/"+id;
    }

    @PostMapping("/post/comment/{id}")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto commentDto, HttpServletRequest request){
        SessionMember loginMember = getLoginMember(request);
        commentService.addComment(id, commentDto, loginMember);

        return "redirect:/post/"+id;
    }

    @PostMapping("/post/{postId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId, HttpServletRequest request){
        SessionMember loginMember = getLoginMember(request);
        try{
            commentService.deleteComment(postId, commentId, loginMember);
        }
        catch(IllegalArgumentException e){

        }
        return "redirect:/post/" + postId;
    }


    private SessionMember getLoginMember(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        SessionMember loginMember = null;

        if (session != null) {
            loginMember = (SessionMember) session.getAttribute("loginMember");
        }
        return loginMember;
    }
}
