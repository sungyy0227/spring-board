package spring.board.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.board.chat.domain.ChatMessage;
import spring.board.chat.domain.ChatRoom;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatRoomOrderByIdAsc(
            ChatRoom chatRoom
    );
}
