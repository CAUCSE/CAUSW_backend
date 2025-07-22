package net.causw.adapter.persistence.chat;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.uuidFile.joinEntity.JoinEntity;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_chat_room_profile_uuid_file",
        indexes = {
                @Index(name = "idx_chat_room_profile_chat_room_id", columnList = "chat_room_id"),
                @Index(name = "idx_chat_room_profile_uuid_file_id", columnList = "uuid_file_id")
        })
public class ChatRoomProfileImage extends JoinEntity {

    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    public static ChatRoomProfileImage of(ChatRoom chatRoom, UuidFile uuidFile) {
        return ChatRoomProfileImage.builder()
                .chatRoom(chatRoom)
                .uuidFile(uuidFile)
                .build();
    }
}


