package spring.board.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import spring.board.dto.EditorImageResponse;
import spring.board.dto.PostDto;
import spring.board.domain.Image;
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
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

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

    public void deletePost(Long id, String password, Long loginMemberId){
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        Member loginMember=null;

        if(loginMemberId!=null){ //1. 관리자일 경우 무조건 삭제
            loginMember = memberRepository.findById(loginMemberId).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            if(loginMember.isAdmin()){
                deletePostImages(post.getId());
                postRepository.delete(post);
                return;
            }
        }

        if(post.getMember()!=null){ //2. 게시물 작성자가 회원일 경우
            if (loginMember==null) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }
            if(!post.getMember().getId().equals(loginMember.getId())){
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
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

        return postRepository.findPostDetailById(id).orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
    }

    public Post getPost(long id){
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    public List<Post> getAllPost(){
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public void deleteAllPost(){
        postRepository.deleteAll();
    }

    public void modifyPost(long id, PostDto postdto, Long loginMemberId, String draftToken, Long verifiedPostId){
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        PolicyFactory policy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES);
        String sanitizedContent = sanitizeContent(policy, postdto.getContent());
        //권한 검증용
        validateUpdatePermission(loginMemberId, verifiedPostId, post);
        boolean hasExistingImage = hasReferencedPostImage(post.getId(), sanitizedContent);

        //수정은 비밀번호 필요 X
        if(post.getMember()==null){ //비회원이 작성했던 글
            validatePostDto(postdto, sanitizedContent, hasExistingImage, true, false);
            post.setPoster(postdto.getPoster());
        }
        else{ //회원이 작성했던 글
            validatePostDto(postdto, sanitizedContent, hasExistingImage, false, false);
        }

        int connectedImageCount = connectImagesToPost(
                postdto.getImageIds(), post, loginMemberId, draftToken, sanitizedContent);
        validateFinalContent(sanitizedContent, hasExistingImage || connectedImageCount > 0);
        post.setTitle(postdto.getTitle());
        post.setContent(sanitizedContent);
    }

    // postDto 입력값과 정제된 본문, 기존 이미지 존재 여부를 함께 검증
    private void validatePostDto(PostDto postDto, String sanitizedContent, boolean hasExistingImage,
                                 boolean requirePoster, boolean requireGuestPassword){
        if (!StringUtils.hasText(postDto.getTitle())) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (!hasTextContent(sanitizedContent) && !hasUploadedImage(postDto) && !hasExistingImage) {
            throw new IllegalArgumentException("내용 또는 이미지는 필수입니다.");
        }
        if (requirePoster && !StringUtils.hasText(postDto.getPoster())) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
        if (requireGuestPassword && !StringUtils.hasText(postDto.getGuestPassword())) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }

    private boolean hasUploadedImage(PostDto postDto) {
        return postDto.getImageIds() != null
                && !postDto.getImageIds().isEmpty();
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

    private String sanitizeContent(PolicyFactory policy, String content) {
        return policy.sanitize(content == null ? "" : content);
    }

    private void validateFinalContent(String content, boolean hasReferencedImage) {
        if (!hasTextContent(content) && !hasReferencedImage) {
            throw new IllegalArgumentException("내용 또는 이미지는 필수입니다.");
        }
    }

    private boolean hasReferencedPostImage(Long postId, String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }

        return imageRepository.findByPostId(postId).stream()
                .anyMatch(image -> content.contains(image.getUrl()));
    }


    public void deleteAllAndResetId() {
        commentRepository.deleteAllNative();
        commentRepository.resetId();

        postRepository.deleteAllNative();
        postRepository.resetId();
    }

    public ResetResult resetBoardData() {
        List<String> imageUrls = imageRepository.findAll().stream()
                .map(Image::getUrl)
                .toList();

        long imageCount = imageUrls.size();
        long commentCount = commentRepository.count();
        long postCount = postRepository.count();

        imageRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();

        deleteImageFilesAfterCommit(imageUrls);

        return new ResetResult(imageCount, commentCount, postCount);
    }

    private void deleteImageFilesAfterCommit(List<String> imageUrls) {
        Runnable fileCleanup = () -> imageUrls.forEach(imageUrl -> {
            try {
                imageService.deleteByImageUrl(imageUrl);
            } catch (RuntimeException exception) {
                log.error("게시판 초기화 후 이미지 파일 삭제에 실패했습니다. imageUrl={}", imageUrl, exception);
            }
        });

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fileCleanup.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileCleanup.run();
            }
        });
    }

    public record ResetResult(long imageCount, long commentCount, long postCount) {
    }

    public Long uploadPost(Long loginMemberId, PostDto postDto, String draftToken) throws IOException {

        Post post = new Post();
        PolicyFactory policy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.IMAGES);
        String sanitizedContent = sanitizeContent(policy, postDto.getContent());

        if (loginMemberId !=null){ //작성자가 회원일때
            Member member = memberRepository.findById(loginMemberId).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            validatePostDto(postDto, sanitizedContent, false, false, false);
            post.setPoster(member.getNickname());
            post.setMember(member);
        }
        else{ //작성자가 비회원일때
            validatePostDto(postDto, sanitizedContent, false, true, true);
            post.setPoster(postDto.getPoster());
            post.setGuestPassword(passwordEncoder.encode(postDto.getGuestPassword()));
        }

        post.setTitle(postDto.getTitle());
        post.setContent(sanitizedContent);
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
        int connectedImageCount = connectImagesToPost(
                postDto.getImageIds(), post, loginMemberId, draftToken, sanitizedContent);
        validateFinalContent(sanitizedContent, connectedImageCount > 0);

        return post.getId();
    }

    private int connectImagesToPost(List<Long> imageIds, Post post, Long loginMemberId,
                                    String draftToken, String content) {
        if (imageIds == null || imageIds.isEmpty()) {
            return 0;
        }

        List<Image> images = imageRepository.findAllById(imageIds);

        if (images.size() != imageIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 이미지가 포함되어 있습니다.");
        }

        int connectedImageCount = 0;

        for (Image image : images) {
            if (canConnectImage(image, loginMemberId, draftToken)) {
                if (content.contains(image.getUrl())) {
                    image.setPost(post);
                    connectedImageCount++;
                }
                continue;
            }

            throw new IllegalArgumentException("이미지 매핑 권한이 없습니다.");
        }

        return connectedImageCount;
    }

    public List<EditorImageResponse> getPendingEditorImages(List<Long> imageIds, Long loginMemberId,
                                                             String draftToken) {
        if (imageIds == null || imageIds.isEmpty()) {
            return List.of();
        }

        return imageRepository.findAllById(imageIds).stream()
                .filter(image -> canConnectImage(image, loginMemberId, draftToken))
                .map(image -> new EditorImageResponse(image.getId(), image.getUrl()))
                .toList();
    }

    private boolean canConnectImage(Image image, Long loginMemberId, String draftToken) {
        if(image.getPost()!=null){
            return false;
        }
        if(loginMemberId!=null){
            Member uploaderMember = image.getUploaderMember();

            if(uploaderMember==null){
                return false;
            }
            if(uploaderMember.getId().equals(loginMemberId)){
                return true;
            }

            return false;
        }

        if(draftToken==null){
            return false;
        }
        if(draftToken.isBlank()){
            return false;
        }
        if (image.getDraftToken() == null) {
            return false;
        }

        if (image.getDraftToken().equals(draftToken)) {
            return true;
        }

        return false;
    }

    //유저가 쓴 글들 조회
    public List<Post> findPostsByMemberId(Long memberId){
        return postRepository.findByMemberId(memberId);
    }

    //수정 페이지 요청용 권한 확인
    public Post validateUpdatePageAccess(Long loginMemberId, Long postId, String guestPassword){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        if (post.getMember() != null) { //게시물 작성자가 회원일경우
            if (loginMemberId == null || !loginMemberId.equals(post.getMember().getId())) {
                throw new IllegalArgumentException("해당 게시물에 대한 수정 권한이 존재하지 않습니다.");
            }
        }
        else{ //비회원일경우
            if(guestPassword==null || !passwordEncoder.matches(guestPassword,post.getGuestPassword())){
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        return post;
    }

    //수정 요청용 권한 확인
    private void validateUpdatePermission(Long loginMemberId,Long verifiedPostId, Post post){
        //회원일경우
        if(post.getMember()!=null){
            if(loginMemberId == null || !loginMemberId.equals(post.getMember().getId())){
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
