package spring.board.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import spring.board.member.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_room_invite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_invite_token",
                        columnNames = "token"
                ),
                @UniqueConstraint(
                        name = "uk_chat_room_invite_room",
                        columnNames = "room_id"
                )
        }
)
public class ChatRoomInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_invite_room")
    )
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_room_invite_created_by")
    )
    private Member createdBy;

    @Column(nullable = false, length = 12)
    private String token;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected ChatRoomInvite() {
    }

    public ChatRoomInvite(
            ChatRoom chatRoom,
            Member createdBy,
            String token,
            LocalDateTime expiresAt
    ) {
        this.chatRoom = chatRoom;
        this.createdBy = createdBy;
        this.token = token;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public Member getCreatedBy() {
        return createdBy;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void reissue(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
