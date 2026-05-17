package spring.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.dto.EditorImageResponse;
import spring.board.domain.Post;
import spring.board.dto.CommentDto;
import spring.board.dto.PostDto;
import spring.board.dto.SessionMember;
import spring.board.service.CommentService;
import spring.board.service.ImageService;
import spring.board.service.MemberService;
import spring.board.service.PostService;

import java.io.IOException;


@Controller
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final ImageService imageService;

    @Autowired
    public PostController(PostService postService, CommentService commentService, MemberService memberService, ImageService imageService) {
        this.postService = postService;
        this.commentService=commentService;
        this.memberService = memberService;
        this.imageService = imageService;
    }

    @RequestMapping("/")
    public String home(@RequestParam(defaultValue = "1") int page,Model model, HttpServletRequest request){
        System.out.println(request.getRemoteAddr() + " 접속");
        HttpSession session=request.getSession(false);
        if(page<1) page=1;
        Page<Post> postPage = postService.getPostPage(page);
        addPostList(model, postPage, page, false);

        if(session!=null){
            SessionMember loginMember=(SessionMember) session.getAttribute("loginMember");
            model.addAttribute("loginMember", loginMember);
        }

        return "index";
    }

    @GetMapping("/posts/new")
    public String postWrite(Model model, HttpServletRequest request){
        SessionMember loginMember = getLoginMember(request);
        model.addAttribute("loginMember", loginMember);

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

    @PostMapping("/posts")
    public String uploadPost(PostDto postdto, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes){
        SessionMember loginMember = getLoginMember(request);
        try{
            Long postId=postService.uploadPost(loginMember,postdto);
            return "redirect:/posts/" + postId;
        }
        catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/posts/new";
        }
    }

    @PostMapping("/editor/images")
    @ResponseBody
    public EditorImageResponse uploadEditorImage(@RequestParam("imageFile")MultipartFile imageFile) throws IOException {
        return imageService.uploadImage(imageFile);
    }

    @GetMapping("/posts/{id}")
    public String showPost(@PathVariable Long id,Model model, HttpServletRequest request){
        Post post=postService.getPostAndIncreaseViewCount(id);
        SessionMember loginMember = getLoginMember(request);
        model.addAttribute("post",post);
        model.addAttribute("commentDto",new CommentDto());
        model.addAttribute("loginMember", loginMember);
        return "postView";
    }

    @DeleteMapping("/posts/{id}")
    public String deletePost(@PathVariable Long id,
                          HttpServletRequest request,@RequestParam(required = false) String password){
        SessionMember loginMember = getLoginMember(request);
        postService.deletePost(id, password, loginMember);
        return "redirect:/";
    }

    @PostMapping("/posts/{id}/edit")
    public String modifyPageRequestPost(@PathVariable Long id, Model model, HttpServletRequest request, @RequestParam(required = false) String password){
        SessionMember loginMember = getLoginMember(request);
        postService.validateUpdatePageAccess(loginMember, id, password);
        if (loginMember == null) {
            HttpSession session = request.getSession();
            session.setAttribute("guestEditVerifiedPostId", id);
        }

        Post post=postService.getPost(id);
        PostDto postdto=new PostDto();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("post", post);
        model.addAttribute("postdto", postdto);
        return "postModify";
    }

    @PatchMapping("/posts/{id}")
    public String modifyRequestPost(@PathVariable Long id, PostDto postdto, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        SessionMember loginMember = getLoginMember(request);
        Long verifiedPostId = null;
        if (session != null) {
            verifiedPostId = (Long) session.getAttribute("guestEditVerifiedPostId");
        }
        postService.validateUpdatePermission(loginMember,id,verifiedPostId);
        postService.modifyPost(id, postdto);
        if(verifiedPostId!=null) session.removeAttribute("guestEditVerifiedPostId");

        return "redirect:/posts/"+id;
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto commentDto, HttpServletRequest request){
        SessionMember loginMember = getLoginMember(request);
        commentService.addComment(id, commentDto, loginMember);

        return "redirect:/posts/"+id;
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId, RedirectAttributes redirectAttributes,
                                HttpServletRequest request, @RequestParam(required = false) String guestRawPassword){
        SessionMember loginMember = getLoginMember(request);
        try{
            commentService.deleteComment(postId, commentId, loginMember, guestRawPassword);
        }
        catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    private SessionMember getLoginMember(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        SessionMember loginMember = null;

        if (session != null) {
            loginMember = (SessionMember) session.getAttribute("loginMember");
        }
        return loginMember;
    }

    @GetMapping("/posts")
    public String searchPost(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page, Model model, HttpServletRequest request){
        if(page<1) page=1;
        Page<Post> postPage = postService.searchPosts(type, keyword, page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        try{
            addPostList(model,postPage,page,true);
        }
        catch (IllegalArgumentException e){
            home(1, model, request);
            model.addAttribute("searchErrorMessage", e.getMessage());
            model.addAttribute("searchMode", false);
        }

        return "index";
    }

    private void addPostList(Model model,Page<Post> postPage,int page, boolean searchMode){
        if (postPage.isEmpty()) {
            model.addAttribute("noSearchResult", true);
        }
        int currentPage = page;
        int totalPages = postPage.getTotalPages();
        int startPage;
        int endPage;

        if (currentPage <= 9) {
            startPage = 1;
            endPage = Math.min(9, totalPages);
        } else {
            startPage = ((currentPage - 10) / 10) * 10 + 10;
            endPage = Math.min(startPage + 9, totalPages);
        }
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("searchMode", searchMode);

    }
}
