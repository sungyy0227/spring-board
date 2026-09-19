package spring.board.chat.dto;

import spring.board.chat.domain.ChatRoomMember;

public record ChatRoomJoinResponse(
        Long roomId
) {
    public static ChatRoomJoinResponse from(ChatRoomMember chatRoomMember) {
        return new ChatRoomJoinResponse(chatRoomMember.getChatRoom().getId());
    }
}
