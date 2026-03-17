package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.board.domain.Comment;
import spring.board.repository.CommentRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository){
        this.commentRepository=commentRepository;
    }

    public Long addComment(Comment comment){
        commentRepository.save(comment);
        return comment.getId();
    }

    public void deleteComment(Long postId,Long commentId){
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(commentId+"번 댓글이 존재하지 않습니다."));

        Long ownerPostId=comment.getPost().getId();
        if(!ownerPostId.equals(postId)){
            throw new IllegalArgumentException("postId랑 commentId가 일치하지 않습니다. postId="+postId+"commentId="+commentId);
        }

        commentRepository.delete(comment);
    }
}
