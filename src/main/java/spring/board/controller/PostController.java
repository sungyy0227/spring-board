package spring.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.board.dto.EditorImageErrorResponse;
import spring.board.dto.EditorImageResponse;
import spring.board.domain.Post;
import spring.board.dto.CommentDto;
import spring.board.dto.PostDto;
import spring.board.security.CustomUserDetails;
import spring.board.service.CommentService;
import spring.board.service.ImageService;
import spring.board.service.MemberService;
import spring.board.service.PostService;
import java.io.IOException;
import java.util.UUID;


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
    public String home(@RequestParam(defaultValue = "1") int page, Model model, HttpServletRequest request){
        if(page<1) page=1;
        addHomeModel(page,model,false);

        return "index";
    }

    private void addHomeModel(int page, Model model, boolean searchMode) {
        if (page < 1) {
            page = 1;
        }

        Page<Post> postPage = postService.getPostPage(page);
        addPostList(model, postPage, page, searchMode);
    }

    private void addLoginMember(Model model, CustomUserDetails loginMember) {
        if (loginMember != null) {
            model.addAttribute("loginMember", loginMember);
        }
    }

    @GetMapping("/posts/new")
    public String postWrite(Model model, @AuthenticationPrincipal CustomUserDetails loginMember){
        if(loginMember!=null){
            model.addAttribute("loginMember", loginMember);
        }

        if (!model.containsAttribute("postDto")) {
            model.addAttribute("postDto",new PostDto());
        }
        return "postWrite";
    }

    @PostMapping("/clearPost")
    public String delAllPost(){ //TODO: 개선 or 삭제
        postService.deleteAllAndResetId();
        return "redirect:/";
    }

    @PostMapping("/clearAll")
    public String delAllPostAndMember(){ //TODO: 개선 or 삭제
        postService.deleteAllAndResetId();
        memberService.resetAllMember();
        return "redirect:/";
    }

    @PostMapping("/posts")
    public String uploadPost(@ModelAttribute("postDto") PostDto postDto,
                             HttpServletRequest request,RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal CustomUserDetails loginMember) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String draftToken = null;

        HttpSession session = request.getSession(false);
        if(session!=null){
            draftToken = (String) session.getAttribute("postDraftToken");
        }

        try{
            Long postId=postService.uploadPost(loginMemberId,postDto, draftToken);

            if(session!=null){
                session.removeAttribute("postDraftToken");
            }

            return "redirect:/posts/" + postId;
        }
        catch(Exception e){
            redirectAttributes.addFlashAttribute("postErrorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("postDto", postDto);
            return "redirect:/posts/new";
        }
    }

    @PostMapping("/editor/images")
    @ResponseBody
    public ResponseEntity<?> uploadEditorImage(@RequestParam("imageFile")MultipartFile imageFile,
                                               @AuthenticationPrincipal CustomUserDetails loginMember,
                                               HttpServletRequest request) throws IOException {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String draftToken = null;

        if(loginMemberId==null){
            HttpSession session = request.getSession();
            draftToken = getOrCreateDraftToken(session);
        }

        try{
            EditorImageResponse response = imageService.uploadImage(imageFile, loginMemberId, draftToken);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(new EditorImageErrorResponse(e.getMessage()));
        }
        catch (IOException e){
            return ResponseEntity.internalServerError().body(new EditorImageErrorResponse("이미지 파일 저장에 실패했습니다."));
        }
        catch (RuntimeException exception) {
            return ResponseEntity.internalServerError().body(new EditorImageErrorResponse("이미지 업로드 처리에 실패했습니다."));
        }
    }

    private String getOrCreateDraftToken(HttpSession session) {
        String draftToken = (String) session.getAttribute("postDraftToken");

        if (draftToken == null) {
            draftToken = UUID.randomUUID().toString();
            session.setAttribute("postDraftToken", draftToken);
        }

        return draftToken;
    }

    @GetMapping("/posts/{id}")
    public String showPost(@PathVariable Long id,Model model,
                           @AuthenticationPrincipal CustomUserDetails loginMember){
        Post post=postService.getPostAndIncreaseViewCount(id);

        model.addAttribute("post",post);
        model.addAttribute("commentDto",new CommentDto());
        model.addAttribute("loginMember", loginMember);
        return "postView";
    }

    @DeleteMapping("/posts/{id}")
    public String deletePost(@PathVariable Long id,
                          @RequestParam(required = false) String password,
                          RedirectAttributes redirectAttributes,@AuthenticationPrincipal CustomUserDetails loginMember){
        Long loginMemberId = loginMember == null ? null : loginMember.getId();

        try{
            postService.deletePost(id, password, loginMemberId);
        }
        catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/posts/" + id;
        }
        return "redirect:/";
    }

    @PostMapping("/posts/{id}/edit")
    public String modifyPageRequestPost(@PathVariable Long id, Model model, HttpServletRequest request,
                                        @RequestParam(required = false) String password,
                                        @AuthenticationPrincipal CustomUserDetails loginMember){
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        Post post;
        try{
            post=postService.validateUpdatePageAccess(loginMemberId, id, password);
        }catch (IllegalArgumentException e){
            Post foundPost = postService.getPost(id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("post", foundPost);
            model.addAttribute("commentDto", new CommentDto());
            model.addAttribute("loginMember", loginMember);
            return "postView";
        }

        if (post.getMember()==null) {
            //비회원이 작성한 글은 수정 페이지 요청시 이미 비밀번호에 대한 검증을 맞췄기 때문에
            //실제 수정 요청시 비밀번호에 대한 검증을 통과했음을 식별 할 수 있는 세션값임
            //만약 없을시 다른 유저가 patch요청을 직접 날려 글 수정 가능
            HttpSession session = request.getSession();
            session.setAttribute("guestEditVerifiedPostId", id);
        }

        PostDto postDto=new PostDto();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("post", post);
        model.addAttribute("postDto", postDto);
        return "postModify";
    }

    @PatchMapping("/posts/{id}")
    public String modifyRequestPost(@PathVariable Long id,
                                    @ModelAttribute("postDto") PostDto postDto,
                                    HttpServletRequest request,
                                    Model model,
                                    @AuthenticationPrincipal CustomUserDetails loginMember){
        HttpSession session = request.getSession(false);

        Long verifiedPostId = null;
        if (session != null) {
            verifiedPostId = (Long) session.getAttribute("guestEditVerifiedPostId");
        }

        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        try{
            String draftToken = null;
            if (session != null) {
                draftToken = (String) session.getAttribute("postDraftToken");
            }
            postService.modifyPost(id, postDto, loginMemberId, draftToken,verifiedPostId);
        }
        catch(IllegalArgumentException e){
            model.addAttribute("errorMessage", e.getMessage());
            Post post=postService.findPost(id);
            model.addAttribute("post", post);
            return "postModify";
        }

        if (verifiedPostId != null) {
            session.removeAttribute("guestEditVerifiedPostId");
        }

        if (session != null) {
            session.removeAttribute("postDraftToken");
        }

        return "redirect:/posts/"+id;
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto commentDto, HttpServletRequest request,
                             RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal CustomUserDetails loginMember){
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        try{
            commentService.addComment(id, commentDto, loginMemberId);
        }
        catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("commentErrorMessage", e.getMessage());
        }

        return "redirect:/posts/"+id;
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId, RedirectAttributes redirectAttributes,
                                @RequestParam(required = false) String guestRawPassword,
                                @AuthenticationPrincipal CustomUserDetails loginMember){
        Long loginMemberId = loginMember == null ? null : loginMember.getId();

        try{
            commentService.deleteComment(postId, commentId, loginMemberId, guestRawPassword);
        }
        catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/posts")
    public String searchPost(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page, Model model){
        if(page<1) page=1;

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        try{
            Page<Post> postPage = postService.searchPosts(type, keyword, page);
            addPostList(model,postPage,page,true);
        }
        catch (IllegalArgumentException e){
            addHomeModel(1,model,false);
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
