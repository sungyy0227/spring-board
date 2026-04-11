package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring.board.dto.PostDto;
import spring.board.dto.SessionMember;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.repository.CommentRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.util.List;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Autowired
    public PostService(PostRepository postRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder, MemberRepository memberRepository){
        this.commentRepository= commentRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
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
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다"));
    }

    public List<Post> getAllPost(){
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public void deleteAllPost(){
        postRepository.deleteAll();
    }

    public void modifyPost(long id, PostDto postdto){
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        if(post.getMember()==null){
            post.setPoster(postdto.getPoster());
        }
        post.setTitle(postdto.getTitle());
        post.setContent(postdto.getContent());
    }

    public void deleteAllAndResetId() {
        commentRepository.deleteAllNative();
        commentRepository.resetId();

        postRepository.deleteAllNative();
        postRepository.resetId();
    }

    public void uploadPost(SessionMember loginMember, PostDto postDto) {
        Post post = new Post();

        if (loginMember!=null){
            post.setPoster(loginMember.getNickname());
            Member member = memberRepository.findById(loginMember.getId()).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            post.setMember(member);
        }
        else{
            post.setPoster(postDto.getPoster());
            post.setGuestPassword(passwordEncoder.encode(postDto.getGuestPassword()));
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        postRepository.save(post);
    }

    //유저가 쓴 글들 조회
    public List<Post> findPostsByMemberId(Long memberId){
        return postRepository.findByMemberId(memberId);
    }

    //수정 페이지 요청용 권한 확인
    public void validateUpdatePageAccess(SessionMember loginMember, Long postId, String guestPassword){
        //회원일경우
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        if(post.getMember()!=null){
            if(loginMember == null || !loginMember.getId().equals(post.getMember().getId())){
                throw new IllegalArgumentException("해당 게시물에 대한 수정 권한이 존재하지 않습니다.");
            }
        }//비회원일경우
        else{
            if(guestPassword==null || !passwordEncoder.matches(guestPassword,post.getGuestPassword())){
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }
    }

    //수정 요청용 권한 확인
    public void validateUpdatePermission(SessionMember loginMember,Long postId, Long verifiedPostId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        //회원일경우
        if(post.getMember()!=null){
            if(loginMember == null || !loginMember.getId().equals(post.getMember().getId())){
                throw new IllegalArgumentException("해당 게시물에 대한 수정 권한이 존재하지 않습니다.");
            }
        }//비회원일경우
        else{
            if(verifiedPostId==null || !post.getId().equals(verifiedPostId)){
                throw new IllegalArgumentException("해당 게시물에 대한 수정 권한이 존재하지 않습니다.");
            }
        }
    }

}
