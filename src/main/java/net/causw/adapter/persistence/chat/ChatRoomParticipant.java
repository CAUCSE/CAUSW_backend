package net.causw.adapter.persistence.chat;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.chat.ParticipantRole;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "tb_chat_room_participant",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_chatroom_user",
            columnNames = {"chat_room_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_room_id", columnList = "chat_room_id")
    }
)
public class ChatRoomParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.MEMBER;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    public static ChatRoomParticipant of(User user, ParticipantRole role) {
        return ChatRoomParticipant.builder()
                .user(user)
                .role(role)
                .build();
    }

    public void updateLastReadAt() {
        this.lastReadAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void pin() {
        this.pinnedAt = LocalDateTime.now();
    }

    public void unpin() {
        this.pinnedAt = null;
    }

    protected void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
} 