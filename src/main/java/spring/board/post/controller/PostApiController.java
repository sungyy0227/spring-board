package spring.board.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import spring.board.post.domain.Post;
import spring.board.post.dto.PostDetailResponse;
import spring.board.post.dto.PostPageResponse;
import spring.board.post.service.PostService;

@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {
    private final PostService postService;

    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostPageResponse getPosts(@RequestParam(defaultValue = "1") int page) {
        int normalizedPage = Math.max(page, 1);
        Page<Post> postPage = postService.getPostPage(normalizedPage);

        return PostPageResponse.from(postPage);
    }

    @GetMapping("/{postId}") //
    public PostDetailResponse getPost(@PathVariable Long postId){
        Post post = postService.getPostAndIncreaseViewCount(postId);

        return PostDetailResponse.from(post);
    }


}
