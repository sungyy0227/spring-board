package spring.board.chat.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import spring.board.chat.repository.ChatRoomMemberRepository;
import spring.board.security.CustomUserDetails;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatWebSocketAuthorizationInterceptor implements ChannelInterceptor {
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/chat/rooms/(\\d+)$");
    private static final Pattern ROOM_SEND = Pattern.compile("^/app/chat/rooms/(\\d+)/send$");

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public ChatWebSocketAuthorizationInterceptor(ChatRoomMemberRepository chatRoomMemberRepository) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == StompCommand.SUBSCRIBE) {
            authorizeRoom(accessor, ROOM_TOPIC);
        } else if (command == StompCommand.SEND) {
            authorizeRoom(accessor, ROOM_SEND);
        }

        return message;
    }

    private void authorizeRoom(StompHeaderAccessor accessor, Pattern allowedDestination) {
        String destination = accessor.getDestination();
        Matcher matcher = destination == null ? null : allowedDestination.matcher(destination);
        if (matcher == null || !matcher.matches()) {
            throw new AccessDeniedException("허용되지 않은 채팅 목적지입니다.");
        }

        if (!(accessor.getUser() instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof CustomUserDetails loginMember)
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        Long roomId = Long.valueOf(matcher.group(1));
        if (!chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(roomId, loginMember.getId())) {
            throw new AccessDeniedException("채팅방에 대한 접근 권한이 없습니다.");
        }
    }
}
