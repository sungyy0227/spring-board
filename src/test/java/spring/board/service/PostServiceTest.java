package spring.board.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.domain.Role;
import spring.board.domain.Status;;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void guestPostCanBeDeletedWithCorrectPassword() {
        Post post = saveGuestPost("1234");

        postService.deletePost(post.getId(), "1234", null);

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    void guestPostCannotBeDeletedWithWrongPassword() {
        Post post = saveGuestPost("1234");

        assertThatThrownBy(() -> postService.deletePost(post.getId(), "wrong", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호 틀림");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    void memberPostCanBeDeletedByOwner() {
        Member owner = saveMember("owner", "owner nickname");
        Post post = saveMemberPost(owner);

        postService.deletePost(post.getId(), null, owner.getId());

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    void memberPostCannotBeDeletedByOtherMember() {
        Member owner = saveMember("owner", "owner nickname");
        Member other = saveMember("other", "other nickname");
        Post post = saveMemberPost(owner);

        assertThatThrownBy(() -> postService.deletePost(post.getId(), null, other.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한 없음");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    void memberPostCannotBeDeletedByGuest() {
        Member owner = saveMember("owner", "owner nickname");
        Post post = saveMemberPost(owner);

        assertThatThrownBy(() -> postService.deletePost(post.getId(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한 없음");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    void adminCanbeDeleteAnyPost(){
        Member admin = saveMember("adminId", "adminNickname");
        admin.setRole(Role.ADMIN);

        Member owner = saveMember("owner", "owner nickname");
        Post post=saveMemberPost(owner);

        postService.deletePost(post.getId(), null, admin.getId());
        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    private Post saveGuestPost(String rawPassword) {
        Post post = new Post();
        post.setTitle("test title");
        post.setContent("test content");
        post.setPoster("guest");
        post.setGuestPassword(passwordEncoder.encode(rawPassword));
        return postRepository.save(post);
    }

    private Post saveMemberPost(Member member) {
        Post post = new Post();
        post.setTitle("member post title");
        post.setContent("member post content");
        post.setPoster(member.getNickname());
        post.setMember(member);
        return postRepository.save(post);
    }

    private Member saveMember(String loginId, String nickname) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode("password"));
        member.setNickname(nickname);
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        return memberRepository.save(member);
    }

}
