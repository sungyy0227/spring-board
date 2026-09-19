package spring.board.chat.dto;

import spring.board.chat.domain.ChatRoomInvite;

import java.time.LocalDateTime;

public record ChatRoomInviteResponse(
        String inviteUrl,
        LocalDateTime expiresAt
) {
    public static ChatRoomInviteResponse from(ChatRoomInvite invite) {
        return new ChatRoomInviteResponse(
                "/chat/invites/" + invite.getToken(),
                invite.getExpiresAt()
        );
    }
}
