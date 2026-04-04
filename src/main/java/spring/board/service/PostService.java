package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring.board.controller.PostDto;
import spring.board.controller.SessionMember;
import spring.board.domain.Post;
import spring.board.repository.CommentRepository;
import spring.board.repository.PostRepository;

import java.util.List;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PostService(PostRepository postRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder){
        this.commentRepository= commentRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long join(Post post){
        postRepository.save(post);
        return post.getId();
    }

    public void deletePost(Long id, String password, SessionMember loginMember){
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시물 없음"));

        if(loginMember!=null && "admin".equals(loginMember.getRole())){ //1. 관리자일 경우 무조건 삭제
            postRepository.delete(post);
            return;
        }

        if(post.getMember()!=null){ //2. 게시물 작성자가 회원일 경우
            if (loginMember == null) {
                throw new IllegalArgumentException("삭제 권한 없음");
            }
            if(!post.getMember().getId().equals(loginMember.getId())){
                throw new IllegalArgumentException("삭제 권한 없음");
            }
        }
        else{ //3. 게시물 작성자가 게스트(비로그인)일 경우
            if(password ==null || !passwordEncoder.matches(password,post.getGuestPassword())){
                throw new IllegalArgumentException("비밀번호 틀림");
            }
        }

        postRepository.delete(post);
    }

    public Post getPostAndIncreaseViewCount(long id){
        Post post=postRepository.findById(id).orElseThrow();
        post.setViewCount(post.getViewCount()+1);

        return post;
    }

    public Post getPost(long id){
        postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 객체가 존재하지 않습니다"));
        return postRepository.findById(id).get();
    }

    public List<Post> getAllPost(){
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public void deleteAllPost(){
        postRepository.deleteAll();
    }

    public void modifyPost(long id, PostDto postdto){
        Post post=postRepository.findById(id).orElseThrow();
        post.setTitle(postdto.getTitle());
        post.setContent(postdto.getContent());
        post.setPoster(postdto.getPoster());
    }

    public void deleteAllAndResetId() {
        commentRepository.deleteAllNative();
        commentRepository.resetId();

        postRepository.deleteAllNative();
        postRepository.resetId();
    }
}
