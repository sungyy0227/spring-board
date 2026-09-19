package spring.board.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRoomCreateRequest(
        @NotBlank(message = "채팅방 이름을 입력해주세요.")
        @Size(max = 100, message = "채팅방 이름은 100자 이하여야 합니다.")
        String name
) {
}
