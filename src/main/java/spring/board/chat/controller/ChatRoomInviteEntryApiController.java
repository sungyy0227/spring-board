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
import spring.board.chat.domain.ChatRoomMember;
import spring.board.chat.dto.ChatRoomInvitePreviewResponse;
import spring.board.chat.dto.ChatRoomJoinResponse;
import spring.board.chat.service.ChatService;
import spring.board.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/chat/invites")
public class ChatRoomInviteEntryApiController {
    private final ChatService chatService;

    public ChatRoomInviteEntryApiController(ChatService chatService){
        this.chatService = chatService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ChatRoomInvitePreviewResponse> getInvite(
            @PathVariable String token) {
        ChatRoomInvite chatRoomInvite=chatService.getChatRoomInviteByInviteToken(token);
        return ResponseEntity.ok(ChatRoomInvitePreviewResponse.from(chatRoomInvite));
    }

    @PostMapping("/{token}/join")
    public ResponseEntity<ChatRoomJoinResponse> joinChatRoom(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails loginMember) {
        ChatRoomMember chatRoomMember = chatService.joinChatRoomByInviteToken(token, loginMember.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ChatRoomJoinResponse.from(chatRoomMember));
    }
}
