package spring.board.member.dto;

import spring.board.comment.domain.Comment;
import spring.board.member.domain.Member;
import spring.board.post.domain.Post;
import spring.board.post.dto.PostSummaryResponse;

import java.util.List;

public record MemberActivityResponse(
        MemberResponse member,
        List<PostSummaryResponse> posts,
        List<MemberCommentResponse> comments
) {
    public static MemberActivityResponse from(Member member, List<Post> posts, List<Comment> comments) {
        return new MemberActivityResponse(
                MemberResponse.from(member),
                posts.stream().map(PostSummaryResponse::from).toList(),
                comments.stream().map(MemberCommentResponse::from).toList()
        );
    }
}
