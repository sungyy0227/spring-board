package spring.board.service;

import jakarta.transaction.Transactional;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring.board.dto.PostDto;
import spring.board.domain.Image;
import spring.board.dto.SessionMember;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.repository.CommentRepository;
import spring.board.repository.ImageRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       PasswordEncoder passwordEncoder, MemberRepository memberRepository, ImageRepository imageRepository, ImageService imageService){
        this.commentRepository= commentRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }

    public void deletePost(Long id, String password, SessionMember loginMember){
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        if(loginMember!=null && loginMember.isAdmin()){ //1. 관리자일 경우 무조건 삭제
            deletePostImages(post.getId());
            postRepository.delete(post);
            return;
        }

        if(post.getMember()!=null){ //2. 게시물 작성자가 회원일 경우
            if (loginMember == null) {
                throw new IllegalArgumentException("삭제 권한 없습니다.");
            }
            if(!post.getMember().getId().equals(loginMember.getId())){
                throw new IllegalArgumentException("삭제 권한 없습니다.");
            }
        }
        else{ //3. 게시물 작성자가 게스트(비로그인)일 경우
            if(password ==null || !passwordEncoder.matches(password,post.getGuestPassword())){
                throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            }

        }

        deletePostImages(post.getId());
        postRepository.delete(post);
    }

    public void deletePostImages(Long postId){
        List<Image> images = imageRepository.findByPostId(postId);
        for (Image image : images){
            imageService.deleteByImageUrl(image.getUrl());
        }
        imageRepository.deleteAll(images);

    }

    public Post getPostAndIncreaseViewCount(Long id){
        int updatedCount = postRepository.increaseViewCount(id);
        if(updatedCount ==0) throw new IllegalArgumentException("게시물이 존재하지 않습니다.");

        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
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
        PolicyFactory policy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES);

        if(post.getMember()==null){
            if(!StringUtils.hasText(postdto.getPoster())){
                throw new IllegalArgumentException("작성자는 필수입니다.");
            }
            post.setPoster(postdto.getPoster());
        }

        validateContent(postdto.getContent());
        connectImagesToPost(postdto.getImageIds(), post);
        post.setTitle(postdto.getTitle());
        post.setContent(policy.sanitize(postdto.getContent()));
    }

    private void validateContent(String content){
        if(!hasTextContent(content)){
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }

    private boolean hasTextContent(String content) {
        if (content == null) {
            return false;
        }

        String text = content
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", "")
                .replace("&#160;", " ")
                .replace("\u00A0", " ")
                .trim();

        return StringUtils.hasText(text);
    }


    public void deleteAllAndResetId() {
        commentRepository.deleteAllNative();
        commentRepository.resetId();

        postRepository.deleteAllNative();
        postRepository.resetId();
    }

    public Long uploadPost(SessionMember loginMember, PostDto postDto) throws IOException {
        Post post = new Post();
        PolicyFactory policy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES);

        if (loginMember!=null){
            post.setPoster(loginMember.getNickname());
            Member member = memberRepository.findById(loginMember.getId()).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            post.setMember(member);
        }
        else{
            post.setPoster(postDto.getPoster());
            post.setGuestPassword(passwordEncoder.encode(postDto.getGuestPassword()));
        }
        try{
            post.setTitle(postDto.getTitle());
            post.setContent(policy.sanitize(postDto.getContent()));
            post.setCreatedAt(LocalDateTime.now());
            postRepository.save(post);
            connectImagesToPost(postDto.getImageIds(), post);
        }
        catch(Exception e){
            throw e;
        }

        return post.getId();
    }

    private void connectImagesToPost(List<Long> imageIds, Post post) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        List<Image> images = imageRepository.findAllById(imageIds);
        for (Image image : images) {
            if(image.getPost()==null){
                image.setPost(post);
            }
        }
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

    public Page<Post> getPostPage(int page){
        Pageable pageable = PageRequest.of(
                page-1, 10, Sort.by(Sort.Direction.DESC, "id")
        );
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchPosts(String type, String keyword, int page){
        if(keyword==null){
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        keyword = keyword.trim();

        if(keyword.length() < 2){
            throw new IllegalArgumentException("검색어를 2글자 이상 입력해주세요.");
        }

        if(type==null) {
            type = "title_content";
        }

        Pageable pageable = PageRequest.of(
                page-1, 10, Sort.by(Sort.Direction.DESC, "id")
        );
        Page<Post> posts;

        switch (type){
            case "title_content":
                posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
                break;
            case "title":
                posts = postRepository.findByTitleContaining(keyword, pageable);
                break;
            case "content":
                posts = postRepository.findByContentContaining(keyword, pageable);
                break;
            case "poster":
                posts = postRepository.findByPosterContaining(keyword, pageable);
                break;
            default:
                posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        }

        return posts;

    }

    public Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
    }
}
