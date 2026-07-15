package spring.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.domain.Role;
import spring.board.domain.Status;
import spring.board.dto.PostDto;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.io.IOException;
import java.util.stream.Stream;

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

    //삭제 -  성공(맞는비번, 회원글셀프삭제, 관리자), 실패(틀린비번, 다른회원이쓴글(회원,비회원시도))
    //생성
    @Test
    @DisplayName("회원 글 작성에 성공한다")
    void memberPostUploadSucceeds() throws IOException {
        Member member=saveMember("testId","tester");
        PostDto postDto = new PostDto();
        postDto.setContent("testContent");
        postDto.setTitle("testTitle");

        Long postId=postService.uploadPost(member.getId(), postDto,null);
        Post savedPost = postRepository.findById(postId).orElseThrow();

        assertThat(savedPost.getMember().getId()).isEqualTo(member.getId());
        assertThat(savedPost.getPoster()).isEqualTo(member.getNickname());
        assertThat(savedPost.getGuestPassword()).isNull();
    }

    @Test
    @DisplayName("비회원 글 작성에 성공한다")
    void guestPostUploadSucceeds() throws IOException {
        String guestNickname = "guest"; //정상값
        String guestPassword = "123456"; //정상값
        PostDto postDto = new PostDto();
        postDto.setContent("testContent"); //정상값
        postDto.setTitle("testTitle"); //정상값
        postDto.setGuestPassword(guestPassword);
        postDto.setPoster(guestNickname);

        Long postId = postService.uploadPost(null, postDto, null);
        Post post = postRepository.findById(postId).orElseThrow();

        assertThat(post.getPoster()).isEqualTo(guestNickname);
        assertThat(passwordEncoder.matches(guestPassword, post.getGuestPassword())).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidGuestPostDtos")
    @DisplayName("유효하지 않은 값으로 글 작성시 실패한다")
    void uploadFailsWithInvalidValues(PostDto postDto, String expectedMessage) {
        assertThatThrownBy(() -> postService.uploadPost(null, postDto,null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    @DisplayName("내용에 태그가 있어도 텍스트가 있으면 글 작성에 성공한다")
    void uploadSucceedsWhenContentHasTagsAndText() throws IOException {
        PostDto postDto = guestPostDto("title", "<p>content</p>", "guest", "123456");

        Long postId = postService.uploadPost(null, postDto,null);
        Post post = postRepository.findById(postId).orElseThrow();

        assertThat(post.getContent()).contains("content");
    }

    private static Stream<Arguments> invalidGuestPostDtos() {
        return Stream.of(
                Arguments.of(guestPostDto(null, "content", "guest", "123456"), "제목은 필수입니다."),
                Arguments.of(guestPostDto("title", null, "guest", "123456"), "내용은 필수입니다."),
                Arguments.of(guestPostDto("title", "<p><br></p>", "guest", "123456"), "내용은 필수입니다."),
                Arguments.of(guestPostDto("title", "content", null, "123456"), "작성자는 필수입니다."),
                Arguments.of(guestPostDto("title", "content", "guest", null), "비밀번호는 필수입니다.")
        );
    }

    private static PostDto guestPostDto(String title, String content, String poster, String guestPassword) {
        PostDto postDto = new PostDto();
        postDto.setTitle(title);
        postDto.setContent(content);
        postDto.setPoster(poster);
        postDto.setGuestPassword(guestPassword);
        return postDto;
    }



    @Test
    @DisplayName("올바른 비밀번호로 비회원 작성 글 삭제가 가능하다")
    void guestPostCanBeDeletedWithCorrectPassword() {
        Post post = saveGuestPost("1234");

        postService.deletePost(post.getId(), "1234", null);

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("틀린 비밀번호로는 비회원 작성 글을 삭제하지 못한다")
    void guestPostCannotBeDeletedWithWrongPassword() {
        Post post = saveGuestPost("1234");

        assertThatThrownBy(() -> postService.deletePost(post.getId(), "wrong", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    @DisplayName("자신이 작성한 글은 삭제가 가능하다")
    void memberPostCanBeDeletedByOwner() {
        Member owner = saveMember("owner", "owner nickname");
        Post post = saveMemberPost(owner);

        postService.deletePost(post.getId(), null, owner.getId());

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 회원이 작성한 글은 삭제하지 못한다")
    void memberPostCannotBeDeletedByOtherMember() {
        Member owner = saveMember("owner", "owner nickname");
        Member other = saveMember("other", "other nickname");
        Post post = saveMemberPost(owner);

        assertThatThrownBy(() -> postService.deletePost(post.getId(), null, other.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    @DisplayName("회원이 작성한 글은 비회원이 삭제하지 못한다")
    void memberPostCannotBeDeletedByGuest() {
        Member owner = saveMember("owner", "owner nickname");
        Post post = saveMemberPost(owner);

        assertThatThrownBy(() -> postService.deletePost(post.getId(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    @DisplayName("관리자는 어떤 글이든 삭제가 가능하다")
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
