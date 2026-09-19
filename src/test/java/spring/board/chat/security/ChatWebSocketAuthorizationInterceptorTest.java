package spring.board.chat.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import spring.board.chat.repository.ChatRoomMemberRepository;
import spring.board.member.domain.Member;
import spring.board.member.domain.Role;
import spring.board.member.domain.Status;
import spring.board.security.CustomUserDetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatWebSocketAuthorizationInterceptorTest {

    private final ChatRoomMemberRepository chatRoomMemberRepository = mock(ChatRoomMemberRepository.class);
    private final MessageChannel channel = mock(MessageChannel.class);
    private ChatWebSocketAuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new ChatWebSocketAuthorizationInterceptor(chatRoomMemberRepository);
    }

    @Test
    void memberCanSubscribeToOwnRoomTopic() {
        when(chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(7L, 11L)).thenReturn(true);

        Message<byte[]> message = stompMessage(
                StompCommand.SUBSCRIBE,
                "/topic/chat/rooms/7",
                authenticatedMember(11L)
        );

        assertDoesNotThrow(() -> interceptor.preSend(message, channel));
    }

    @Test
    void nonMemberCannotSubscribeToRoomTopic() {
        when(chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(7L, 11L)).thenReturn(false);

        Message<byte[]> message = stompMessage(
                StompCommand.SUBSCRIBE,
                "/topic/chat/rooms/7",
                authenticatedMember(11L)
        );

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void clientCannotSendDirectlyToBrokerTopic() {
        Message<byte[]> message = stompMessage(
                StompCommand.SEND,
                "/topic/chat/rooms/7",
                authenticatedMember(11L)
        );

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void memberCanSendOnlyThroughRoomApplicationDestination() {
        when(chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(7L, 11L)).thenReturn(true);

        Message<byte[]> message = stompMessage(
                StompCommand.SEND,
                "/app/chat/rooms/7/send",
                authenticatedMember(11L)
        );

        assertDoesNotThrow(() -> interceptor.preSend(message, channel));
    }

    @Test
    void nonMemberCannotSendToRoomApplicationDestination() {
        when(chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(7L, 11L)).thenReturn(false);

        Message<byte[]> message = stompMessage(
                StompCommand.SEND,
                "/app/chat/rooms/7/send",
                authenticatedMember(11L)
        );

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, channel));
    }

    private Message<byte[]> stompMessage(StompCommand command, String destination,
                                         UsernamePasswordAuthenticationToken authentication) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setUser(authentication);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private UsernamePasswordAuthenticationToken authenticatedMember(Long memberId) {
        Member member = new Member();
        member.setId(memberId);
        member.setLoginId("member-" + memberId);
        member.setPassword("password");
        member.setNickname("member-" + memberId);
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
    }
}
