package spring.board.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomInvite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomInviteRepository extends JpaRepository<ChatRoomInvite, Long> {

    Optional<ChatRoomInvite> findByToken(String token);

    Optional<ChatRoomInvite> findByChatRoomAndExpiresAtAfter(ChatRoom chatroom, LocalDateTime now);

    Optional<ChatRoomInvite> findByChatRoom(ChatRoom chatRoom);
}
