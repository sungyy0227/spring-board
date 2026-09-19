package spring.board.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.board.chat.domain.ChatRoomInvite;
import spring.board.chat.dto.ChatRoomInviteResponse;
import spring.board.chat.service.ChatService;
import spring.board.security.CustomUserDetails;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/chat/rooms/{roomId}/invites")
public class ChatRoomInviteApiController {

    private final ChatService chatService;

    public ChatRoomInviteApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/active")
    public ResponseEntity<ChatRoomInviteResponse> getActiveInvite(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails loginMember) {
        Optional<ChatRoomInvite> chatRoomInvite=chatService.getActiveInvite(roomId, loginMember.getId());
        if (chatRoomInvite.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                ChatRoomInviteResponse.from(chatRoomInvite.get())
        );
    }

    @PostMapping
    public ResponseEntity<ChatRoomInviteResponse> createInvite(@PathVariable Long roomId,@AuthenticationPrincipal CustomUserDetails loginMember) {
        ChatRoomInvite chatRoomInvite=chatService.createInvite(roomId, loginMember.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ChatRoomInviteResponse.from(chatRoomInvite));
    }

    @PostMapping("/rotate")
    public ResponseEntity<ChatRoomInviteResponse>  rotateInvite(@PathVariable Long roomId,@AuthenticationPrincipal CustomUserDetails loginMember) {
        ChatRoomInvite chatRoomInvite = chatService.rotateInvite(roomId, loginMember.getId());

        return ResponseEntity.ok(ChatRoomInviteResponse.from(chatRoomInvite));
    }
}
