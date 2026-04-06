package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.board.controller.CommentDto;
import spring.board.controller.SessionMember;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.repository.CommentRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, MemberRepository memberRepository){
        this.commentRepository=commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    public Long addComment(Long id, CommentDto commentDto, SessionMember loginMember) {

        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        Comment comment = new Comment();
        if(loginMember!=null){
            Member member = memberRepository.findById(loginMember.getId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            comment.setCommenter(member.getNickname());
            comment.setMember(member);
        }
        else{
            comment.setCommenter(commentDto.getCommenter());
        }
        comment.setCommentContent(commentDto.getCommentContent());
        post.addComment(comment);

        commentRepository.save(comment);
        return comment.getId();
    }

    public void deleteComment(Long postId,Long commentId, SessionMember loginMember){
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(commentId+"번 댓글이 존재하지 않습니다."));

        Long ownerPostId=comment.getPost().getId();
        //게시물, 댓글이 같은 게시물에서 왔는지 확인
        if(!ownerPostId.equals(postId)){
            throw new IllegalArgumentException("postId랑 commentId가 일치하지 않습니다. postId="+postId+"commentId="+commentId);
        }
        //로그인 상태확인
        if(loginMember==null) {
            throw new IllegalArgumentException("로그인 상태가 아닙니다.");
        }
        //관리자 여부 확인
        if ("admin".equals(loginMember.getRole())) {
            commentRepository.delete(comment);
            return;
        }
        //댓글 작성자랑 삭제 요청 사용자 일치 여부 확인
        if (comment.getMember() == null || !loginMember.getId().equals(comment.getMember().getId())) {
            throw new IllegalArgumentException("해당 댓글의 작성자가 아닙니다.");
        }


        commentRepository.delete(comment);
    }
}
