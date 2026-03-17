package spring.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import spring.board.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM MEMBER", nativeQuery = true)
    void deleteAllNative();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "ALTER TABLE MEMBER ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
    void resetId();

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByNickname(String nickname);

    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
}
