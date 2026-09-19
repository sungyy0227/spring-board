package spring.board.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomMember;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findAllByMember_IdOrderByJoinedAtDesc(Long memberId);

    Optional<ChatRoomMember> findByChatRoom_IdAndMember_Id(
            Long roomId,
            Long memberId
    );

    boolean existsByChatRoom_IdAndMember_Id(
            Long roomId,
            Long memberId
    );
}
