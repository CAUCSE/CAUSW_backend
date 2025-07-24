package net.causw.app.main.service.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;
import net.causw.app.main.domain.model.entity.chat.ChatRoomParticipant;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.chat.ChatRoomParticipantDto;
import net.causw.app.main.infrastructure.redis.chat.ChatRedisService;
import net.causw.app.main.repository.chat.ChatMessageRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomParticipantService chatRoomParticipantService;
	private final ChatRedisService redisService;

	@Transactional
	public ChatMessage saveMessage(ChatMessageDto.SendMessageCommand command, String senderId) {
		List<String> fileIds = command.getUuidFiles() != null
			? command.getUuidFiles().stream().map(UuidFile::getId).toList()
			: null;

		ChatMessage message = ChatMessage.builder()
			.roomId(command.getRoomId())
			.senderId(senderId)
			.content(command.getContent())
			.messageType(command.getMessageType())
			.fileIds(fileIds)
			.timestamp(LocalDateTime.now())
			.build();

		return chatMessageRepository.save(message);
	}

	@Transactional
	public ChatMessageDto.MessagePageResponse getMessagesWithParticipants(
		String roomId, LocalDateTime before, int limit, String userId) {

		List<ChatMessage> messages = chatMessageRepository
			.findByRoomIdAndTimestampBeforeOrderByTimestampDesc(roomId,
				before != null ? before : LocalDateTime.now(),
				PageRequest.of(0, limit));

		List<ChatRoomParticipant> participants = chatRoomParticipantService.findAllByRoomId(roomId);

		ChatRoomParticipant currentParticipant = participants.stream()
			.filter(participant -> userId.equals(participant.getId())).findFirst()
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.CHAT_ROOM_PARTICIPANT_NOT_FOUND));

		List<ChatRoomParticipantDto.ParticipantResponse> participantsDto = participants.stream().map(participant -> {
			User user = participant.getUser();

			return ChatRoomParticipantDto.ParticipantResponse.of(
				participant.getId(),
				user.getName(),
				user.getUserProfileImage().uuidFile.getFileUrl(),
				participant.getUpdatedAt()
			);
		}).toList();

		redisService.clearUnreadCount(userId, roomId);
		chatRoomParticipantService.updateLastReadAt(currentParticipant);

		return ChatMessageDto.MessagePageResponse.builder()
			.participants(participantsDto)
			.messages(messages.stream().map(ChatMessageDto.MessageResponse::from).toList())
			.hasNext(messages.size() == limit)
			.build();
	}

	public void deleteMessageByMessageId(String messageId, String userId) {
		ChatMessage message = findByMessageId(messageId);

		if (!message.getSenderId().equals(userId)) {
			throw new BadRequestException(ErrorCode.API_NOT_ALLOWED, MessageUtil.CHAT_MESSAGE_DELETE_NOT_ACCESSIBLE);
		}

		message.setIsDeleted(true);
		chatMessageRepository.save(message);
	}

	public void deleteMessageByRoomId(String roomId) {
		chatMessageRepository.deleteByRoomId(roomId);
	}

	public ChatMessage findByMessageId(String messageId) {
		return chatMessageRepository.findById(messageId).orElseThrow(() -> new BadRequestException(
			ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CHAT_MESSAGE_NOT_FOUND));
	}

	public ChatMessage findByRoomIdOrderByTimestampDesc(String roomId) {
		return chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId).orElse(null);
	}
}

