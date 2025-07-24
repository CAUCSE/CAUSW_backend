package net.causw.app.main.dto.chat.chat;


import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.enums.chat.MessageType;
import net.causw.app.main.dto.chat.chat.ChatRoomDto.RoomDetail;
import net.causw.app.main.dto.chat.chat.ChatRoomParticipantDto.ParticipantResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatMessageDto {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FirstOneToOneMessageRequest {
		@NotEmpty
		private String targetUserId;

		@NotBlank
		private String content;

		@NotNull
		private MessageType messageType;

		private String postId;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FirstGroupMessageRequest {
		@NotEmpty
		private List<String> targetUserIds;

		@NotBlank
		private String content;

		@NotNull
		private MessageType messageType;

		@NotBlank
		private String roomName;
	}

	@Getter
	@Setter
	@Builder(access = AccessLevel.PRIVATE)
	public static class FirstMessageResponse {
		private RoomDetail room;
		private List<ParticipantResponse> participants;
		private MessageResponse message;

		public static FirstMessageResponse from(
			RoomDetail room,
			List<ParticipantResponse> participants,
			MessageResponse message
		) {
			return FirstMessageResponse.builder()
				.room(room)
				.participants(participants)
				.message(message)
				.build();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder(access = AccessLevel.PRIVATE)
	public static class SendMessageRequest {
		@NotBlank
		private String roomId;

		private String content;

		@NotNull
		private MessageType messageType;

		public static SendMessageRequest of(String roomId, String content, MessageType messageType) {
			return SendMessageRequest.builder()
				.roomId(roomId)
				.content(content)
				.messageType(messageType)
				.build();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder(access = AccessLevel.PRIVATE)
	public static class SendMessageCommand {
		private String roomId;
		private String content;
		private MessageType messageType;
		private List<UuidFile> uuidFiles;

		public static SendMessageCommand of(String roomId, String content, MessageType messageType,
			List<UuidFile> uuidFiles) {
			return SendMessageCommand.builder()
				.roomId(roomId)
				.content(content)
				.messageType(messageType)
				.uuidFiles(uuidFiles)
				.build();
		}

		public static SendMessageCommand from(SendMessageRequest request, List<UuidFile> uuidFiles) {
			return SendMessageCommand.builder()
				.roomId(request.getRoomId())
				.content(request.getContent())
				.messageType(request.getMessageType())
				.uuidFiles(uuidFiles)
				.build();
		}

		public static SendMessageCommand from(SendMessageRequest request) {
			return SendMessageCommand.builder()
				.roomId(request.getRoomId())
				.content(request.getContent())
				.messageType(request.getMessageType())
				.uuidFiles(null)
				.build();
		}
	}

	@Getter
	@Setter
	@Builder
	public static class MessageResponse {
		private String id;
		private String senderId;
		private String content;
		private MessageType messageType;
		private LocalDateTime timestamp;
		private List<String> fileUrls;

		public static MessageResponse from(ChatMessage message, List<UuidFile> uuidFiles) {
			MessageResponse response = MessageResponse.builder()
				.id(message.getId())
				.senderId(message.getSenderId())
				.content(message.getContent())
				.messageType(message.getMessageType())
				.timestamp(message.getTimestamp())
				.build();

			List<String> fileUrls = uuidFiles.stream().map(UuidFile::getFileUrl).toList();
			response.setFileUrls(fileUrls);

			return response;
		}
	}

	@Getter
	@Setter
	@Builder(access = AccessLevel.PRIVATE)
	public static class PreviewMessageResponse {
		private String content;
		private String senderName;
		private MessageType messageType;
		private LocalDateTime timestamp;

		public static PreviewMessageResponse from(ChatMessage message) {
			return PreviewMessageResponse.builder()
				.content(message.getContent())
				.senderName(message.getSenderName())
				.messageType(message.getMessageType())
				.timestamp(message.getTimestamp())
				.build();
		}
	}

	@Getter
	@Setter
	@Builder
	public static class MessagePageResponse {
		private List<ParticipantResponse> participants;
		private List<MessageResponse> messages;
		private boolean hasNext;
	}
}