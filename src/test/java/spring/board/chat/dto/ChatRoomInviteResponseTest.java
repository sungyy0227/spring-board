package spring.board.chat.dto;

import org.junit.jupiter.api.Test;
import spring.board.chat.domain.ChatRoomInvite;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRoomInviteResponseTest {

    @Test
    void fromBuildsRelativeInviteUrlAndPreservesExpiration() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 20, 12, 30);
        ChatRoomInvite invite = new ChatRoomInvite(
                null,
                null,
                "k7Fv2aQ9_xLm",
                expiresAt
        );

        ChatRoomInviteResponse response = ChatRoomInviteResponse.from(invite);

        assertEquals("/chat/invites/k7Fv2aQ9_xLm", response.inviteUrl());
        assertEquals(expiresAt, response.expiresAt());
    }
}
