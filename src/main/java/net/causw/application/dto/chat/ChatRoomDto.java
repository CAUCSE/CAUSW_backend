package net.causw.application.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.domain.model.enums.chat.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

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