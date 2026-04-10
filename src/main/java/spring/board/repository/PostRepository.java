package spring.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import spring.board.domain.Post;

import java.util.*;

public interface PostRepository extends JpaRepository<Post,Long> {

//    public Post save(Post post);
//
//    public void deleteById(long id);
//
//    public Optional<Post> findById(long id);
//
//    public List<Post> findAll();
//
//    public void clear();
//
//    public void update(Post post);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM POST", nativeQuery = true)
    void deleteAllNative();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "ALTER TABLE POST ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
    void resetId();

    List<Post> findByMemberId(Long memberId);
}
