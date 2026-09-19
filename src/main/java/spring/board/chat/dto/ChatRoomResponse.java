package spring.board.chat.dto;

import spring.board.chat.domain.ChatRoom;

public record ChatRoomResponse(
        Long id,
        String name
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName()
        );
    }
}
