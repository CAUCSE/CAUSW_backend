package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.chat.ChatMessage;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.application.dto.chat.ChatMessageDto;
import net.causw.application.dto.chat.ChatRoomDto;
import net.causw.application.dto.chat.ChatRoomParticipantDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

