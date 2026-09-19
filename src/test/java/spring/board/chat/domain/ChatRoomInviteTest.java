package spring.board.chat.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRoomInviteTest {

    @Test
    void reissueChangesTokenAndExpirationTogether() {
        ChatRoomInvite invite = new ChatRoomInvite(
                null,
                null,
                "oldToken1234",
                LocalDateTime.of(2026, 9, 20, 12, 0)
        );
        LocalDateTime newExpiresAt = LocalDateTime.of(2026, 10, 1, 12, 0);

        invite.reissue("newToken5678", newExpiresAt);

        assertEquals("newToken5678", invite.getToken());
        assertEquals(newExpiresAt, invite.getExpiresAt());
    }
}
