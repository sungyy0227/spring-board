package spring.board.chat.dto;

import org.junit.jupiter.api.Test;
import spring.board.exception.ApiErrorResponse;
import spring.board.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatInviteErrorResponseTest {

    @Test
    void chatInviteNotFoundProducesExpectedApiError() {
        ApiErrorResponse response = ApiErrorResponse.from(
                ErrorCode.CHAT_INVITE_NOT_FOUND
        );

        assertEquals("CHAT_INVITE_NOT_FOUND", response.code());
        assertEquals("초대 링크가 존재하지 않습니다.", response.message());
    }

    @Test
    void chatMemberNotFoundProducesExpectedApiError() {
        ApiErrorResponse response = ApiErrorResponse.from(
                ErrorCode.CHAT_MEMBER_NOT_FOUND
        );

        assertEquals("CHAT_MEMBER_NOT_FOUND", response.code());
        assertEquals("채팅방 멤버가 존재하지 않습니다.", response.message());
    }

    @Test
    void existingChatInviteProducesExpectedApiError() {
        ApiErrorResponse response = ApiErrorResponse.from(
                ErrorCode.CHAT_INVITE_ALREADY_EXISTS
        );

        assertEquals("CHAT_INVITE_ALREADY_EXISTS", response.code());
        assertEquals("이미 초대 링크가 존재합니다.", response.message());
    }
}
