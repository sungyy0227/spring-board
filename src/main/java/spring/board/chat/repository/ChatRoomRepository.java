package spring.board.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.board.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
