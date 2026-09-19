package spring.board.chat.dto;

import org.junit.jupiter.api.Test;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomMember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatRoomJoinResponseTest {

    @Test
    void fromUsesTheJoinedChatRoomId() {
        ChatRoom chatRoom = mock(ChatRoom.class);
        ChatRoomMember chatRoomMember = mock(ChatRoomMember.class);
        when(chatRoom.getId()).thenReturn(42L);
        when(chatRoomMember.getChatRoom()).thenReturn(chatRoom);

        ChatRoomJoinResponse response = ChatRoomJoinResponse.from(chatRoomMember);

        assertEquals(42L, response.roomId());
    }
}
