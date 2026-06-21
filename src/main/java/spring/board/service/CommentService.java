package spring.board.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring.board.dto.CommentDto;
import spring.board.domain.Comment;
import spring.board.domain.Member;
import spring.board.domain.Post;
import spring.board.repository.CommentRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;

import java.util.List;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.commentRepository=commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long addComment(Long id, CommentDto commentDto,Long loginMemberId) {

        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
        Comment comment = new Comment();
        if(!StringUtils.hasText(commentDto.getCommentContent())){
            throw new IllegalArgumentException("내용은 필수입니다.");
        }

        if(loginMemberId!=null){ //작성자가 회원인 경우
            Member member = memberRepository.findById(loginMemberId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            comment.setCommenter(member.getNickname());
            comment.setMember(member);
        }
        else{ //작성자가 비회원인 경우
            if(!StringUtils.hasText(commentDto.getCommenter())){
                throw new IllegalArgumentException("작성자는 필수입니다.");
            }
            if(!StringUtils.hasText(commentDto.getGuestRawPassword())){
                throw new IllegalArgumentException("비밀번호는 필수입니다.");
            }
            comment.setCommenter(commentDto.getCommenter());
            comment.setGuestPassword(passwordEncoder.encode(commentDto.getGuestRawPassword()));
        }
        comment.setCommentContent(commentDto.getCommentContent());
        post.addComment(comment);

        commentRepository.save(comment);
        return comment.getId();
    }

    public void deleteComment(Long postId,Long commentId, Long loginMemberId, String guestRawPassword){
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(commentId+"번 댓글이 존재하지 않습니다."));

        Long ownerPostId=comment.getPost().getId();
        Member loginMember=null;
        //게시물, 댓글이 같은 게시물에서 왔는지 확인
        if(!ownerPostId.equals(postId)){
            throw new IllegalArgumentException("postId랑 commentId가 일치하지 않습니다. postId="+postId+"commentId="+commentId);
        }
        //관리자일 경우 바로 삭제
        if(loginMemberId!=null){
            loginMember = memberRepository.findById(loginMemberId).orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다."));
            if(loginMember.isAdmin()){
                commentRepository.delete(comment);
                return;
            }
        }
        //댓글 작성자가 회원일경우
        if(comment.getMember() != null ){
            //로그인상태고 작성자랑 삭제 요청 유저가 같은지 확인
            if(loginMember!=null && comment.getMember().getId().equals(loginMemberId)){
                commentRepository.delete(comment);
                return;
            }
        }
        else{
            //게스트고 댓글 작성할때 입력한 패스워드랑 삭제할때 입력한 패스워드 같은지 확인
            if(guestRawPassword!=null && passwordEncoder.matches(guestRawPassword, comment.getGuestPassword())){
                commentRepository.delete(comment);
                return;
            }
        }

        throw new IllegalArgumentException("해당 댓글을 삭제하지 못했습니다.");

    }

    //유저가 쓴 댓글들 조회
    public List<Comment> findCommentsByMemberId(Long memberId){
        return commentRepository.findByMemberId(memberId);
    }
}
