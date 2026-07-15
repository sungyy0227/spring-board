package spring.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.*;
import spring.board.dto.CommentDto;
import spring.board.repository.CommentRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CommentServiceTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("회원 댓글 작성에 성공한다")
    void memberCommentAddSucceeds(){
        Member member = new Member();
        member.setRole(Role.USER);
        member.setPassword(passwordEncoder.encode("123456"));
        member.setStatus(Status.ACTIVE);
        member.setNickname("tester");
        member.setLoginId("test1234");
        memberRepository.save(member);

        Post post = new Post();
        postRepository.save(post);

        CommentDto commentDto = new CommentDto();
        commentDto.setCommentContent("test comment");

        Long commentId = commentService.addComment(post.getId(), commentDto, member.getId());
        Comment savedComment = commentRepository.findById(commentId).orElseThrow();

        assertThat(savedComment.getCommentContent()).isEqualTo("test comment");
        assertThat(savedComment.getCommenter()).isEqualTo(member.getNickname());
        assertThat(savedComment.getMember().getId()).isEqualTo(member.getId());
        assertThat(savedComment.getPost().getId()).isEqualTo(post.getId());
        assertThat(savedComment.getGuestPassword()).isNull();
    }


}
