package net.causw.app.main.domain.model.entity.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import net.causw.app.main.domain.model.enums.chat.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
	private MessageType messageType;

	@Field("content")
	private String content;

	@CreatedDate
	@Field("timestamp")
	private LocalDateTime timestamp;

	@Field("file_ids")
	private List<String> fileIds;

	@Field("is_deleted")
	@Builder.Default
	private Boolean isDeleted = false;

	@LastModifiedDate
	@Field("updated_at")
	private LocalDateTime updatedAt;
}
