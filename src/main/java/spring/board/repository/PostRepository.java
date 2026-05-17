package spring.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.board.domain.Post;

import java.util.*;

public interface PostRepository extends JpaRepository<Post,Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM POST", nativeQuery = true)
    void deleteAllNative();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "ALTER TABLE POST ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
    void resetId();

    List<Post> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    int increaseViewCount(@Param("id") Long id);

    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    Page<Post> findByContentContaining(String keyword, Pageable pageable);

    Page<Post> findByTitleContainingOrContentContaining(
            String titleKeyword,
            String contentKeyword,
            Pageable pageable
    );

    Page<Post> findByPosterContaining(String keyword, Pageable pageable);
}
