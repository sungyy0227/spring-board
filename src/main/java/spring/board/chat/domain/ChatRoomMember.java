package spring.board.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import spring.board.member.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_room_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_room_member_room_member",
                columnNames = {"room_id", "member_id"}
        ),
        indexes = @Index(
                name = "idx_chat_room_member_member_id",
                columnList = "member_id"
        )
)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_member_room")
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_member_member")
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomMemberRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    protected ChatRoomMember() {
    }

    public ChatRoomMember(
            ChatRoom chatRoom,
            Member member,
            ChatRoomMemberRole role
    ) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public Member getMember() {
        return member;
    }

    public ChatRoomMemberRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
