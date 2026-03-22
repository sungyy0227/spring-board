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
    public String uploadPost(PostDto postdto, HttpServletRequest request){ //세션 체크는 나중에 인터셉터로 빼기
        HttpSession session = request.getSession(false);
        SessionMember loginMember = null;

        if (session != null) {
            loginMember = (SessionMember) session.getAttribute("loginMember");
        }

        Post post = new Post();

        if (loginMember != null) {
            post.setPoster(loginMember.getNickname());
            Member member = memberService.findById(loginMember.getId());
            post.setMember(member);
        } else {
            post.setPoster(postdto.getPoster());
            post.setGuestPassword(passwordEncoder.encode(postdto.getGuestPassword()));
        }

        post.setTitle(postdto.getTitle());
        post.setContent(postdto.getContent());

        postService.join(post);
        return "redirect:/";
    }

    @GetMapping("/post/{id}")
    public String showPost(@PathVariable Long id,Model model){
        Post post=postService.getPostAndIncreaseViewCount(id);
        model.addAttribute("post",post);
        model.addAttribute("commentDto",new CommentDto());
        return "postView";
    }

    @PostMapping("/post/delete/{id}")
    public String delPost(@PathVariable Long id, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        SessionMember loginMember = null;
        if (session != null) {
            loginMember = (SessionMember) session.getAttribute("loginMember");
        }

        Post post = postService.getPost(id);
        Member member = post.getMember();

        if(loginMember==null) return "redirect:/login"; //비로그인일 경우 삭제불가

        if (member == null) { //비로그인 작성글이면 회원정보가 없음
            return "redirect:/";
        }

        if (loginMember.getId().equals(member.getId())) {
            postService.deletePost(id);
        }


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
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto commentDto){
        Post post=postService.getPost(id);
        Comment comment=new Comment();
        comment.setCommenter(commentDto.getCommenter());
        comment.setCommentContent(commentDto.getCommentContent());
        post.addComment(comment);
        commentService.addComment(comment);

        return "redirect:/post/"+id;
    }
    @PostMapping("/post/{postId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId){
        commentService.deleteComment(postId,commentId);
        return "redirect:/post/" + postId;
    }
}
