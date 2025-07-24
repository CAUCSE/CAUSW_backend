package net.causw.application.chat;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.chat.ChatMessageDto;
import net.causw.application.dto.chat.ChatRoomDto;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.model.enums.uuidFile.FilePath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatFacadeService {

    private final ChatRoomService chatRoomService;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final ChatMessageService chatMessageService;
    private final ChatMessageSocketService chatMessageSocketService;
    private final UuidFileService uuidFileService;

    @Transactional
    public ChatMessageDto.FirstMessageResponse sendFirstOneToOneMessage(
            ChatMessageDto.FirstOneToOneMessageRequest request,
            List<MultipartFile> messageFileList,
            User sender
    ) {
        List<UuidFile> messageFiles = messageFileList != null
                ? uuidFileService.saveFileList(messageFileList, FilePath.CHAT)
                : null;

        return chatRoomService.sendFirstOneToOneMessage(request, messageFiles, sender);
    }

    @Transactional
    public ChatMessageDto.FirstMessageResponse sendFirstGroupMessage(
            ChatMessageDto.FirstGroupMessageRequest request,
            MultipartFile roomProfileImageFile,
            List<MultipartFile> messageFileList,
            User sender
    ) {
        UuidFile roomProfileImage = uuidFileService.saveFile(roomProfileImageFile, FilePath.CHAT);
        List<UuidFile> messageFiles = messageFileList != null
                ? uuidFileService.saveFileList(messageFileList, FilePath.CHAT)
                : null;

        return chatRoomService.sendFirstGroupMessage(request, roomProfileImage, messageFiles, sender);
    }

    public ChatRoomDto.RoomListResponse getChatRooms(User user, int pageNum, int pageSize) {
        return chatRoomService.getChatRooms(user, pageNum, pageSize);
    }

    public ChatMessageDto.MessagePageResponse getMessagesWithParticipants(
            String roomId,
            LocalDateTime before,
            int limit,
            String userId
    ) {
        return chatMessageService.getMessagesWithParticipants(roomId, before, limit, userId);
    }

    public void pinRoom(String roomId, String userId) {
        chatRoomParticipantService.pinRoom(roomId, userId);
    }

    public void unpinRoom(String roomId, String userId) {
        chatRoomParticipantService.unpinRoom(roomId, userId);
    }

    public void leaveRoom(String roomId, String userId) {
        chatRoomService.leaveRoom(roomId, userId);
    }

    public void deleteMessage(String messageId, String userId) {
        chatMessageService.deleteMessageByMessageId(messageId, userId);
    }

    @Transactional
    public void sendMessage(ChatMessageDto.SendMessageRequest request, User sender) {
        sendMessageInternal(request, null, sender);
    }

    @Transactional
    public ChatMessageDto.MessageResponse sendMessage(
            ChatMessageDto.SendMessageRequest request,
            List<MultipartFile> messageFiles,
            User sender
    ) {
        return sendMessageInternal(request, messageFiles, sender);
    }

    private ChatMessageDto.MessageResponse sendMessageInternal(
            ChatMessageDto.SendMessageRequest request,
            List<MultipartFile> messageFileList,
            User sender
    ) {
        List<UuidFile> messageFiles = messageFileList != null
                ? uuidFileService.saveFileList(messageFileList, FilePath.CHAT)
                : null;

        ChatMessageDto.SendMessageCommand command = ChatMessageDto.SendMessageCommand.from(request, messageFiles);
        ChatRoom chatRoom = chatRoomService.findByRoomIdAndUserId(command.getRoomId(), sender.getId());

        return chatMessageSocketService.processIncomingMessage(command, chatRoom, sender);
    }
}

