package spring.board.chat.dto;

import spring.board.chat.domain.ChatRoomInvite;

import java.time.LocalDateTime;

public record ChatRoomInvitePreviewResponse(String roomName, LocalDateTime expiresAt) {
    public static ChatRoomInvitePreviewResponse from(ChatRoomInvite chatRoomInvite) {
        return new ChatRoomInvitePreviewResponse(chatRoomInvite.getChatRoom().getName(),
                chatRoomInvite.getExpiresAt());
    }
}
