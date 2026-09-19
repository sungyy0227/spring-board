package spring.board.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import spring.board.chat.domain.ChatMessage;
import spring.board.chat.dto.ChatMessageResponse;
import spring.board.chat.dto.ChatSendRequest;
import spring.board.chat.service.ChatService;
import spring.board.security.CustomUserDetails;

import java.security.Principal;

@Controller
public class ChatWebSocketController {
    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/rooms/{roomId}/send")
    @SendTo("/topic/chat/rooms/{roomId}")
    public ChatMessageResponse send(@DestinationVariable Long roomId, ChatSendRequest request, Principal principal) {
        Authentication authentication = (Authentication) principal;

        CustomUserDetails loginMember =
                (CustomUserDetails) authentication.getPrincipal();

        ChatMessage message = chatService.sendMessage(roomId, loginMember.getId(), request.content());

        return ChatMessageResponse.from(message);
    }
}
