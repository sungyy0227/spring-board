package spring.board.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import spring.board.domain.Image;
import spring.board.repository.CommentRepository;
import spring.board.repository.ImageRepository;
import spring.board.repository.MemberRepository;
import spring.board.repository.PostRepository;
import spring.board.service.PostService.ResetResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceResetTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("회원은 유지하고 이미지, 댓글, 게시글을 순서대로 초기화한다")
    void resetBoardDataDeletesImagesCommentsAndPosts() {
        Image firstImage = new Image();
        firstImage.setUrl("/images/post/first.png");
        Image secondImage = new Image();
        secondImage.setUrl("/images/post/second.png");

        when(imageRepository.findAll()).thenReturn(List.of(firstImage, secondImage));
        when(commentRepository.count()).thenReturn(3L);
        when(postRepository.count()).thenReturn(2L);

        ResetResult result = postService.resetBoardData();

        InOrder deleteOrder = inOrder(imageRepository, commentRepository, postRepository);
        deleteOrder.verify(imageRepository).deleteAllInBatch();
        deleteOrder.verify(commentRepository).deleteAllInBatch();
        deleteOrder.verify(postRepository).deleteAllInBatch();

        verify(imageService).deleteByImageUrl("/images/post/first.png");
        verify(imageService).deleteByImageUrl("/images/post/second.png");
        assertThat(result.imageCount()).isEqualTo(2L);
        assertThat(result.commentCount()).isEqualTo(3L);
        assertThat(result.postCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미지 파일은 DB 트랜잭션 커밋 후 삭제한다")
    void resetBoardDataDeletesImageFilesAfterCommit() {
        Image image = new Image();
        image.setUrl("/images/post/after-commit.png");
        when(imageRepository.findAll()).thenReturn(List.of(image));

        TransactionSynchronizationManager.initSynchronization();
        try {
            postService.resetBoardData();

            verifyNoInteractions(imageService);
            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);

            synchronizations.get(0).afterCommit();

            verify(imageService).deleteByImageUrl("/images/post/after-commit.png");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
