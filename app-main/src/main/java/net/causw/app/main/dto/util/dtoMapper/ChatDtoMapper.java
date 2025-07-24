package net.causw.application.dto.util.dtoMapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;
import net.causw.app.main.domain.model.entity.chat.ChatRoom;
import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.chat.ChatRoomDto;
import net.causw.app.main.dto.chat.chat.ChatRoomParticipantDto;

@Mapper(componentModel = "spring")
public interface ChatDtoMapper {

	// --- ChatRoomDto mapping ---
	@Mapping(source = "id", target = "roomId")
	@Mapping(source = "roomName", target = "roomName")
	@Mapping(source = "roomType", target = "roomType")
	@Mapping(target = "lastActivityAt", expression = "java(room.getUpdatedAt())")
	@Mapping(target = "createdAt", expression = "java(room.getCreatedAt())")
	ChatRoomDto.RoomDetail toRoomSummary(
		ChatRoom room,
		@Context String roomName,
		@Context int unreadCount,
		@Context boolean isPinned,
		@Context ChatRoomDto.LastMessageDto lastMessage,
		@Context ChatRoomParticipantDto otherParticipant
	);

	default ChatRoomDto.RoomDetail mapRoomToSummary(
		ChatRoom room,
		String dynamicRoomName,
		int unreadCount,
		boolean isPinned,
		ChatRoomDto.LastMessageDto lastMessage,
		ChatRoomParticipantDto otherParticipant
	) {
		return toRoomSummary(room, dynamicRoomName, unreadCount, isPinned, lastMessage, otherParticipant);
	}

	// --- ChatMessageDto mapping ---
	default ChatMessageDto.MessageResponse toMessageResponse(ChatMessage message) {
		return ChatMessageDto.MessageResponse.from(message);
	}
}

