package spring.board.post.dto;

import org.springframework.data.domain.Page;
import spring.board.post.domain.Post;

import java.util.ArrayList;
import java.util.List;

public record PostPageResponse(
        List<PostSummaryResponse> content,
        int currentPage,
        int totalPages,
        long totalElements
) {
    public static PostPageResponse from(Page<Post> postPage) {
        List<PostSummaryResponse> content = new ArrayList<>();
        for (Post post : postPage.getContent()) {
            PostSummaryResponse response =
                    PostSummaryResponse.from(post);

            content.add(response);
        }


        return new PostPageResponse(
                content,
                postPage.getNumber() + 1,
                postPage.getTotalPages(),
                postPage.getTotalElements()
        );
    }
}
