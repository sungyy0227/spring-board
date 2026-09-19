package spring.board.chat.dto;

import spring.board.chat.domain.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long senderId,
        String senderNickname,
        String content,
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
