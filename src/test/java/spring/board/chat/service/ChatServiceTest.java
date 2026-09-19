package spring.board.chat.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomInvite;
import spring.board.chat.domain.ChatRoomMember;
import spring.board.chat.domain.ChatRoomMemberRole;
import spring.board.chat.repository.ChatRoomInviteRepository;
import spring.board.chat.repository.ChatRoomMemberRepository;
import spring.board.exception.ForbiddenException;
import spring.board.exception.InvalidRequestException;
import spring.board.member.domain.Member;
import spring.board.member.domain.Role;
import spring.board.member.domain.Status;
import spring.board.member.repository.MemberRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private ChatRoomInviteRepository chatRoomInviteRepository;

    @Test
    void createChatRoomAssignsOwnerRoleToCreator() {
        Member creator = new Member();
        creator.setLoginId("chat-owner");
        creator.setPassword("password");
        creator.setNickname("chat-owner");
        creator.setRole(Role.USER);
        creator.setStatus(Status.ACTIVE);
        memberRepository.save(creator);

        ChatRoom chatRoom = chatService.createChatRoom("테스트 채팅방", creator.getId());

        ChatRoomMember roomMember = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(chatRoom.getId(), creator.getId())
                .orElseThrow();
        assertEquals(ChatRoomMemberRole.OWNER, roomMember.getRole());
    }

    @Test
    void memberCannotCreateInvite() {
        Member owner = saveMember("invite-create-owner");
        ChatRoom chatRoom = chatService.createChatRoom("초대 권한 테스트", owner.getId());
        Member member = saveMember("invite-create-member");
        chatRoomMemberRepository.save(new ChatRoomMember(
                chatRoom,
                member,
                ChatRoomMemberRole.MEMBER
        ));

        assertThrows(
                ForbiddenException.class,
                () -> chatService.createInvite(chatRoom.getId(), member.getId())
        );
    }

    @Test
    void memberCannotGetActiveInvite() {
        Member owner = saveMember("invite-read-owner");
        ChatRoom chatRoom = chatService.createChatRoom("초대 조회 권한 테스트", owner.getId());
        chatService.createInvite(chatRoom.getId(), owner.getId());
        Member member = saveMember("invite-read-member");
        chatRoomMemberRepository.save(new ChatRoomMember(
                chatRoom,
                member,
                ChatRoomMemberRole.MEMBER
        ));

        assertThrows(
                ForbiddenException.class,
                () -> chatService.getActiveInvite(chatRoom.getId(), member.getId())
        );
    }

    @Test
    void cannotCreateSecondInviteForSameRoom() {
        Member owner = saveMember("invite-duplicate-owner");
        ChatRoom chatRoom = chatService.createChatRoom("초대 중복 테스트", owner.getId());
        chatService.createInvite(chatRoom.getId(), owner.getId());

        assertThrows(
                InvalidRequestException.class,
                () -> chatService.createInvite(chatRoom.getId(), owner.getId())
        );
    }

    @Test
    void databaseRejectsSecondInviteForSameRoom() {
        Member owner = saveMember("invite-constraint-owner");
        ChatRoom chatRoom = chatService.createChatRoom("초대 제약 테스트", owner.getId());
        chatRoomInviteRepository.saveAndFlush(new ChatRoomInvite(
                chatRoom,
                owner,
                "firstToken01",
                LocalDateTime.now().plusDays(7)
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> chatRoomInviteRepository.saveAndFlush(new ChatRoomInvite(
                        chatRoom,
                        owner,
                        "secondToken2",
                        LocalDateTime.now().plusDays(7)
                ))
        );
    }

    private Member saveMember(String value) {
        Member member = new Member();
        member.setLoginId(value);
        member.setPassword("password");
        member.setNickname(value);
        member.setRole(Role.USER);
        member.setStatus(Status.ACTIVE);
        return memberRepository.save(member);
    }
}
