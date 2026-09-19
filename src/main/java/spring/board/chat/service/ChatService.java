package spring.board.chat.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import spring.board.chat.domain.ChatMessage;
import spring.board.chat.domain.ChatRoom;
import spring.board.chat.domain.ChatRoomInvite;
import spring.board.chat.domain.ChatRoomMember;
import spring.board.chat.domain.ChatRoomMemberRole;
import spring.board.chat.dto.ChatMessageResponse;
import spring.board.chat.dto.ChatRoomResponse;
import spring.board.chat.repository.ChatMessageRepository;
import spring.board.chat.repository.ChatRoomInviteRepository;
import spring.board.chat.repository.ChatRoomMemberRepository;
import spring.board.chat.repository.ChatRoomRepository;
import spring.board.exception.ErrorCode;
import spring.board.exception.ForbiddenException;
import spring.board.exception.InvalidRequestException;
import spring.board.exception.NotFoundException;
import spring.board.member.domain.Member;
import spring.board.member.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomInviteRepository chatRoomInviteRepository;
    private final InviteTokenGenerator inviteTokenGenerator;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository,
                       ChatRoomMemberRepository chatRoomMemberRepository, MemberRepository memberRepository,
                       ChatRoomInviteRepository chatRoomInviteRepository, InviteTokenGenerator inviteTokenGenerator) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.chatRoomInviteRepository = chatRoomInviteRepository;
        this.inviteTokenGenerator = inviteTokenGenerator;
    }

    public ChatMessage sendMessage(Long roomId, Long senderId , String content){
        if(content == null || content.isBlank()){
            throw new InvalidRequestException(ErrorCode.CHAT_MESSAGE_REQUIRED);
        }
        if(content.length()>500){
            throw new InvalidRequestException(ErrorCode.CHAT_MESSAGE_TOO_LONG);
        }

        // 채팅방 존재 여부와 참여 권한을 함께 확인
        ChatRoom chatRoom = getChatRoom(roomId, senderId);

        Member member = memberRepository.findById(senderId).orElseThrow(() ->
                new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        ChatMessage chatMessage = new ChatMessage(chatRoom, member, content);

        return chatMessageRepository.save(chatMessage);

    }

    public List<ChatRoomResponse> getMyChatRooms(Long memberId) {
        return chatRoomMemberRepository.findAllByMember_IdOrderByJoinedAtDesc(memberId)
                .stream()
                .map(chatRoomMember -> ChatRoomResponse.from(chatRoomMember.getChatRoom()))
                .toList();
    }

    public ChatRoom getChatRoom(Long roomId,Long loginMemberId){
        ChatRoomMember roomMember = chatRoomMemberRepository.findByChatRoom_IdAndMember_Id(roomId, loginMemberId)
                .orElseThrow(() ->
                        new ForbiddenException(ErrorCode.CHAT_ROOM_ACCESS_DENIED)
                );

        return roomMember.getChatRoom();
    }

    public List<ChatMessageResponse> getChatMessages(Long roomId, Long memberId){
        ChatRoom chatRoom=getChatRoom(roomId,memberId);

        return chatMessageRepository.findAllByChatRoomOrderByIdAsc(chatRoom).stream().map(ChatMessageResponse::from).toList();
    }

    public ChatRoom createChatRoom(String name, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        ChatRoom chatRoom = new ChatRoom(name);
        chatRoomRepository.save(chatRoom);
        ChatRoomMember chatRoomMember = new ChatRoomMember(
                chatRoom,
                member,
                ChatRoomMemberRole.OWNER
        );
        chatRoomMemberRepository.save(chatRoomMember);

        return chatRoom;
    }


    public Optional<ChatRoomInvite> getActiveInvite(Long roomId, Long memberId) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() ->
                        new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));

        if (chatRoomMember.getRole() != ChatRoomMemberRole.OWNER
                && chatRoomMember.getRole() != ChatRoomMemberRole.MANAGER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }

        ChatRoom chatRoom = chatRoomMember.getChatRoom();

        return chatRoomInviteRepository.findByChatRoomAndExpiresAtAfter(
                chatRoom,
                LocalDateTime.now()
        );
    }

    public ChatRoomInvite createInvite(Long roomId, Long memberId){
        ChatRoomMember chatRoomMember = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() ->
                        new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));

        if (chatRoomMember.getRole() != ChatRoomMemberRole.OWNER
                && chatRoomMember.getRole() != ChatRoomMemberRole.MANAGER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }

        ChatRoom chatRoom = chatRoomMember.getChatRoom();
        Optional<ChatRoomInvite> existingInvite = chatRoomInviteRepository.findByChatRoom(chatRoom);
        LocalDateTime now = LocalDateTime.now();
        if (existingInvite.isPresent()) {
            ChatRoomInvite chatRoomInvite = existingInvite.get();
            if (chatRoomInvite.getExpiresAt().isAfter(now)) {
                throw new InvalidRequestException(ErrorCode.CHAT_INVITE_ALREADY_EXISTS);
            }

            chatRoomInvite.reissue(inviteTokenGenerator.generate(), now.plusDays(7));
            return chatRoomInvite;
        }

        String inviteToken = inviteTokenGenerator.generate();
        Member member = chatRoomMember.getMember();
        ChatRoomInvite chatRoomInvite = new ChatRoomInvite(chatRoom, member, inviteToken, now.plusDays(7));

        chatRoomInviteRepository.save(chatRoomInvite);
        return chatRoomInvite;
    }

    public ChatRoomInvite rotateInvite(Long roomId,Long memberId){
        ChatRoomMember chatRoomMember = chatRoomMemberRepository
                .findByChatRoom_IdAndMember_Id(roomId, memberId)
                .orElseThrow(() ->
                        new NotFoundException(ErrorCode.CHAT_MEMBER_NOT_FOUND));

        if (chatRoomMember.getRole() != ChatRoomMemberRole.OWNER
                && chatRoomMember.getRole() != ChatRoomMemberRole.MANAGER) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }

        ChatRoom chatRoom = chatRoomMember.getChatRoom();
        ChatRoomInvite chatRoomInvite = chatRoomInviteRepository.findByChatRoom(chatRoom)
                .orElseThrow(() ->
                        new NotFoundException(ErrorCode.CHAT_INVITE_NOT_FOUND));

        String inviteNewToken = inviteTokenGenerator.generate();
        chatRoomInvite.reissue(inviteNewToken, LocalDateTime.now().plusDays(7));

        return chatRoomInvite;
    }

    public ChatRoomInvite getChatRoomInviteByInviteToken(String token) {
        ChatRoomInvite chatRoomInvite = chatRoomInviteRepository.findByToken(token).orElseThrow(() ->
                        new NotFoundException(ErrorCode.CHAT_INVITE_NOT_FOUND));
        if (!chatRoomInvite.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new NotFoundException(ErrorCode.CHAT_INVITE_NOT_FOUND);
        }
        return chatRoomInvite;
    }

    public ChatRoomMember joinChatRoomByInviteToken(String token, Long memberId){
        ChatRoomInvite chatRoomInvite = chatRoomInviteRepository.findByToken(token).orElseThrow(() ->
                new NotFoundException(ErrorCode.CHAT_INVITE_NOT_FOUND));
        if (!chatRoomInvite.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new NotFoundException(
                    ErrorCode.CHAT_INVITE_NOT_FOUND
            );
        }

        ChatRoom chatRoom = chatRoomInvite.getChatRoom();

        if (chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(chatRoom.getId(), memberId)) {
            throw new InvalidRequestException(ErrorCode.CHAT_MEMBER_ALREADY_EXISTS);
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        ChatRoomMember chatRoomMember = new ChatRoomMember(
                chatRoomInvite.getChatRoom(),
                member,
                ChatRoomMemberRole.MEMBER
        );

        chatRoomMemberRepository.save(chatRoomMember);
        return chatRoomMember;
    }
}
