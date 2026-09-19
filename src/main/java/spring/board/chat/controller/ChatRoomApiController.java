package spring.board.chat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomMember;
import spring.board.chat.dto.ChatRoomCreateRequest;
import spring.board.chat.dto.ChatRoomResponse;
import spring.board.chat.service.ChatService;
import spring.board.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/rooms")
public class ChatRoomApiController {

    private final ChatService chatService;

    public ChatRoomApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatRoomResponse> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        return chatService.getMyChatRooms(loginMember.getId());
    }

    @GetMapping("/{roomId}")
    public ChatRoomResponse getChatRoom(@PathVariable Long roomId,
                                         @AuthenticationPrincipal CustomUserDetails loginMember){
        ChatRoom chatRoom = chatService.getChatRoom(roomId, loginMember.getId());
        return ChatRoomResponse.from(chatRoom);
    }

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@AuthenticationPrincipal CustomUserDetails loginMember,
                                                           @Valid @RequestBody ChatRoomCreateRequest request){
        ChatRoom chatRoom = chatService.createChatRoom(request.name(), loginMember.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ChatRoomResponse.from(chatRoom));
    }
}
