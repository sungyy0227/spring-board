package spring.board.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import spring.board.post.domain.Post;
import spring.board.post.dto.*;
import spring.board.post.service.PostService;
import spring.board.security.CustomUserDetails;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {
    private final PostService postService;

    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostPageResponse getPosts(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(required = false) String type,
                                     @RequestParam(required = false) String keyword) {
        int normalizedPage = Math.max(page, 1);
        Page<Post> postPage = keyword == null
                ? postService.getPostPage(normalizedPage)
                : postService.searchPosts(type, keyword, normalizedPage);

        return PostPageResponse.from(postPage);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId,
                           @RequestBody(required = false) PostDeleteRequest request,
                           @AuthenticationPrincipal CustomUserDetails loginMember) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String password = request == null ? null : request.password();
        postService.deletePost(postId, password, loginMemberId);
    }

    @PostMapping("/{postId}/edit-access")
    public PostEditResponse verifyEditAccess(
            @PathVariable Long postId,
            @RequestBody(required = false) PostEditAccessRequest request,
            @AuthenticationPrincipal CustomUserDetails loginMember,
            HttpServletRequest httpServletRequest
    ) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        String password = request == null ? null : request.password();
        Post post = postService.validateUpdatePageAccess(loginMemberId, postId, password);

        if (post.getMember() == null) {
            httpServletRequest.getSession(true).setAttribute("guestEditVerifiedPostId", postId);
        }

        return PostEditResponse.from(post);
    }

    @GetMapping("/{postId}/edit")
    public PostEditResponse getPostForEdit(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails loginMember,
            HttpServletRequest httpServletRequest
    ) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        HttpSession session = httpServletRequest.getSession(false);
        Long verifiedPostId = session == null
                ? null
                : (Long) session.getAttribute("guestEditVerifiedPostId");
        return PostEditResponse.from(
                postService.getPostForEdit(loginMemberId, postId, verifiedPostId)
        );
    }

    @PatchMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePost(@PathVariable Long postId,
                           @RequestBody PostUpdateRequest request,
                           @AuthenticationPrincipal CustomUserDetails loginMember,
                           HttpServletRequest httpServletRequest) {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();
        HttpSession session = httpServletRequest.getSession(false);
        String draftToken = session == null
                ? null
                : (String) session.getAttribute("postDraftToken");
        Long verifiedPostId = session == null
                ? null
                : (Long) session.getAttribute("guestEditVerifiedPostId");

        postService.modifyPost(
                postId,
                PostDto.from(request),
                loginMemberId,
                draftToken,
                verifiedPostId
        );

        if (session != null) {
            session.removeAttribute("postDraftToken");
            session.removeAttribute("guestEditVerifiedPostId");
        }
    }

    @GetMapping("/{postId}") //
    public PostDetailResponse getPost(@PathVariable Long postId){
        Post post = postService.getPostAndIncreaseViewCount(postId);

        return PostDetailResponse.from(post);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCreateResponse createPost(@RequestBody PostCreateRequest request,
                                         @AuthenticationPrincipal CustomUserDetails loginMember,
                                         HttpServletRequest httpServletRequest) throws IOException {
        Long loginMemberId = loginMember == null ? null : loginMember.getId();

        String draftToken = null;
        HttpSession session = httpServletRequest.getSession(false);
        if(session!=null){
            draftToken = (String) session.getAttribute("postDraftToken");
        }

        Long postId = postService.uploadPost(loginMemberId, PostDto.from(request), draftToken);
        if(session!=null){
            session.removeAttribute("postDraftToken");
        }

        return PostCreateResponse.from(postId);
    }


}
