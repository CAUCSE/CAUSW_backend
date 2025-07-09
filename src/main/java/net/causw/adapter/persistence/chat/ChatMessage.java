package net.causw.adapter.persistence.chat;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "chat_messages")
@CompoundIndex(name = "room_timestamp_idx", def = "{'room_id': 1, 'timestamp': -1}")
public class ChatMessage {
    @Id
    private String id;

    @Field("room_id")
    private String roomId;

    @Field("sender_id")
    private String senderId;

    @Field("sender_name")
    private String senderName;

    @Field("message_type")
    private String messageType;

    @Field("content")
    private String content;

    @CreatedDate
    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
