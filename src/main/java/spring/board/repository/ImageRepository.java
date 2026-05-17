package spring.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.board.domain.Image;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {
    List<Image> findByPostId(Long postId);
    List<Image> findByPostIsNullAndUploadedAtBefore(LocalDateTime cutoff);
}
