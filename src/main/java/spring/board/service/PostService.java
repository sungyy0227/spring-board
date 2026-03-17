package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import spring.board.controller.PostDto;
import spring.board.domain.Post;
//import spring.board.repository.MemoryPostRepository;
import spring.board.repository.CommentRepository;
import spring.board.repository.PostRepository;

import java.util.List;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public PostService(PostRepository postRepository, CommentRepository commentRepository){
        this.commentRepository= commentRepository;
        this.postRepository = postRepository;
    }

    public Long join(Post post){
        postRepository.save(post);
        return post.getId();
    }

    public void deletePost(Long id){
        postRepository.deleteById(id);
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
