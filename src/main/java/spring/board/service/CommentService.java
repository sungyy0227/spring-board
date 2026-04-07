package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.commentRepository=commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
            comment.setGuestPassword(passwordEncoder.encode(commentDto.getGuestRawPassword()));
        }
        comment.setCommentContent(commentDto.getCommentContent());
        post.addComment(comment);

        commentRepository.save(comment);
        return comment.getId();
    }

    public void deleteComment(Long postId,Long commentId, SessionMember loginMember, String guestRawPassword){
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(commentId+"번 댓글이 존재하지 않습니다."));

        Long ownerPostId=comment.getPost().getId();

        //게시물, 댓글이 같은 게시물에서 왔는지 확인
        if(!ownerPostId.equals(postId)){
            throw new IllegalArgumentException("postId랑 commentId가 일치하지 않습니다. postId="+postId+"commentId="+commentId);
        }
        //관리자일 경우 바로 삭제
        if(loginMember!=null && "admin".equals(loginMember.getRole())){
            commentRepository.delete(comment);
            return;
        }

        if(comment.getMember() != null ){
            //로그인상태고 작성자랑 삭제 요청 유저가 같은지 확인
            if(loginMember!=null && comment.getMember().getId().equals(loginMember.getId())){
                commentRepository.delete(comment);
                return;
            }
        }
        else{
            //게스트고 댓글 작성할때 입력한 패스워드랑 삭제할때 입력한 패스워드 같은지 확인
            if(passwordEncoder.matches(guestRawPassword, comment.getGuestPassword())){
                commentRepository.delete(comment);
                return;
            }
        }

        throw new IllegalArgumentException("해당 댓글을 삭제하지 못했습니다.");

    }
}
