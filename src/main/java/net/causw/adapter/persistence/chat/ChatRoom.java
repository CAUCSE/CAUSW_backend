package net.causw.adapter.persistence.chat;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.chat.ChatRoomType;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_chat_room")
public class ChatRoom extends BaseEntity {

    @Column(name = "name", length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatRoomType roomType;

    @OneToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "chatRoom")
    private ChatRoomProfileImage roomProfileImage;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChatRoomParticipant> participants = new HashSet<>();

    public static ChatRoom of(
            String roomName,
            ChatRoomType roomType
    ) {
        return ChatRoom.builder()
                .roomName(roomName)
                .roomType(roomType)
                .build();
    }

    public void addParticipant(ChatRoomParticipant participant) {
        participant.setChatRoom(this);
        this.participants.add(participant);
    }
} 