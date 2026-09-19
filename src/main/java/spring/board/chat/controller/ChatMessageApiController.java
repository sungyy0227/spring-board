package spring.board.chat.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.board.chat.dto.ChatMessageResponse;
import spring.board.chat.service.ChatService;
import spring.board.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/rooms/{roomId}/messages")
public class ChatMessageApiController {

    private final ChatService chatService;

    public ChatMessageApiController(ChatService chatService){
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatMessageResponse> getMessages(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails loginMember) {
        return chatService.getChatMessages(roomId, loginMember.getId());
    }
}
