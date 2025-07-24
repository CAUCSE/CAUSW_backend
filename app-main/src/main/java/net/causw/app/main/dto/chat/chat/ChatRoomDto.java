package net.causw.app.main.dto.chat.chat;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.model.entity.chat.ChatRoom;
import net.causw.app.main.domain.model.enums.chat.ChatRoomType;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatRoomDto {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder(access = AccessLevel.PRIVATE)
	public static class RoomDetail {
		private String roomId;
		private String roomName;
		private ChatRoomType roomType;
		private String roomProfileUrl;
		private int unreadCount;
		private boolean isPinned;
		private LocalDateTime lastActivityAt;
		private LocalDateTime createdAt;

		public static RoomDetail from(ChatRoom chatRoom) {
			return RoomDetail.builder()
				.roomId(chatRoom.getId())
				.roomName(chatRoom.getRoomName())
				.roomType(chatRoom.getRoomType())
				.roomPr
				.build();
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	private static class RoomWithPreviewMessage {
		private RoomDetail room;
		private ChatMessageDto.PreviewMessageResponse previewMessage;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RoomListResponse {
		private List<RoomWithPreviewMessage> rooms;
		private int totalUnreadCount;
		private boolean hasNext;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoomIdRequest {
		@NotBlank
		private String roomId;
	}
}