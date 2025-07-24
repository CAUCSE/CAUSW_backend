package net.causw.app.main.service.chat;

import java.util.List;

import net.causw.app.main.domain.model.enums.chat.MessageType;
import net.causw.app.main.dto.chat.chat.ChatRoomParticipantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;
import net.causw.app.main.domain.model.entity.chat.ChatRoom;
import net.causw.app.main.domain.model.entity.chat.ChatRoomParticipant;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.ChatRoomProfileImage;
import net.causw.app.main.domain.model.enums.chat.ChatRoomType;
import net.causw.app.main.domain.model.enums.chat.ParticipantRole;
import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.chat.ChatRoomDto;
import net.causw.app.main.infrastructure.redis.chat.ChatRedisService;
import net.causw.app.main.repository.chat.ChatRoomRepository;
import net.causw.app.main.service.post.PostService;
import net.causw.app.main.service.user.UserService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageService chatMessageService;
	private final ChatMessageSocketService chatMessageSocketService;
	private final ChatRoomParticipantService chatRoomParticipantService;
	private final ChatRedisService redisService;
	private final UserService userService;
	private final PostService postService;

	@Transactional
	public ChatMessageDto.FirstMessageResponse sendFirstOneToOneMessage(
			ChatMessageDto.FirstOneToOneMessageRequest request,
			List<UuidFile> messageFiles,
			User sender
	) {
		User target = userService.findByUserId(request.getTargetUserId());
		ChatRoom room = findOneToOneRoom(sender.getId(), target.getId());

		if (room == null) {
			String roomName = request.getPostId() != null
					? postService.findPostById(request.getPostId()).getTitle()
					: target.getName();

			ChatRoom newRoom = ChatRoom.of(roomName, ChatRoomType.ONE_TO_ONE);
			newRoom.addParticipant(ChatRoomParticipant.of(sender, ParticipantRole.ADMIN));
			newRoom.addParticipant(ChatRoomParticipant.of(target, ParticipantRole.MEMBER));

			room = chatRoomRepository.save(newRoom);
		}

		return sendFirstMessageToRoom(room, request.getContent(), request.getMessageType(), messageFiles, sender);
	}

	@Transactional
	public ChatMessageDto.FirstMessageResponse sendFirstGroupMessage(
			ChatMessageDto.FirstGroupMessageRequest request,
			UuidFile roomProfileImageFile,
			List<UuidFile> messageFiles,
			User sender
	) {
		ChatRoom newRoom = ChatRoom.of(request.getRoomName(), ChatRoomType.GROUP);
		newRoom.setRoomProfileImage(ChatRoomProfileImage.of(newRoom, roomProfileImageFile));
		newRoom.addParticipant(ChatRoomParticipant.of(sender, ParticipantRole.ADMIN));

		List<User> targetUsers = userService.findAllById(request.getTargetUserIds());
		targetUsers.forEach(user -> {
			if (!user.getId().equals(sender.getId())) {
				newRoom.addParticipant(ChatRoomParticipant.of(user, ParticipantRole.MEMBER));
			}
		});

		ChatRoom room = chatRoomRepository.save(newRoom);

		return sendFirstMessageToRoom(room, request.getContent(), request.getMessageType(), messageFiles, sender);
	}

	private ChatMessageDto.FirstMessageResponse sendFirstMessageToRoom(
			ChatRoom room,
			String content,
			MessageType messageType,
			List<UuidFile> messageFiles,
			User sender
	) {
		ChatMessageDto.SendMessageCommand command = ChatMessageDto.SendMessageCommand.of(room.getId(), content, messageType, messageFiles);
		ChatMessageDto.MessageResponse message = chatMessageSocketService.processIncomingMessage(command, room, sender);

		List<ChatRoomParticipantDto.ParticipantResponse> participants = room.getParticipants().stream().map(participant -> {
			String profileImageUrl = participant.getUser().getUserProfileImage().getUuidFile().getFileUrl();

			return ChatRoomParticipantDto.ParticipantResponse.of(
					participant.getId(),
					participant.getUser().getName(),
					profileImageUrl,
					participant.getLastReadAt()
			);
		}).toList();

		ChatRoomParticipant senderParticipant = room.getParticipants().stream()
				.filter(participant -> participant.getId().equals(sender.getId()))
				.findFirst()
				.orElseThrow(() -> new BadRequestException(
						ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CHAT_ROOM_PARTICIPANT_NOT_FOUND));

		ChatRoomDto.RoomDetail roomDetail = ChatRoomDto.RoomDetail.builder()
				.roomId(room.getId())
				.roomName(room.getRoomName())
				.roomType(room.getRoomType())
				.roomProfileUrl(room.getRoomProfileImage().getUuidFile().getFileUrl())
				.unreadCount(redisService.getUnreadCount(sender.getId(), room.getId()))
				.isPinned(senderParticipant.isPinned())
				.lastActivityAt(message.getTimestamp())
				.createdAt(room.getCreatedAt())
				.build();

		return ChatMessageDto.FirstMessageResponse.from(roomDetail, participants, message);
	}


	public ChatRoomDto.RoomListResponse getChatRooms(User user, int pageNum, int pageSize) {
		Page<ChatRoom> rooms = chatRoomRepository.findActiveChatRoomsByParticipant(user, PageRequest.of(pageNum, pageSize));

		List<ChatRoomDto.RoomWithPreviewMessage> roomWithPreviewMessages = rooms.stream().map(room -> {
			int unreadCount = redisService.getUnreadCount(user.getId(), room.getId());
			ChatMessage lastMessage = chatMessageService.findByRoomIdOrderByTimestampDesc(room.getId());
			ChatRoomParticipant userParticipant = chatRoomParticipantService.findByRoomIdAndUserId(room.getId(), user.getId());
			ChatRoomDto.RoomDetail roomDetail = ChatRoomDto.RoomDetail.builder()
					.roomId(room.getId())
					.roomName(room.getRoomName())
					.roomType(room.getRoomType())
					.roomProfileUrl(room.getRoomProfileImage().getUuidFile().getFileUrl())
					.unreadCount(unreadCount)
					.isPinned(userParticipant.isPinned())
					.lastActivityAt(lastMessage.getTimestamp())
					.createdAt(room.getCreatedAt())
					.build();
			ChatMessageDto.PreviewMessageResponse previewMessage = ChatMessageDto.PreviewMessageResponse.from(lastMessage);

			return ChatRoomDto.RoomWithPreviewMessage.of(roomDetail, previewMessage);
		}).toList();

		int totalUnread = redisService.getTotalUnreadCount(user.getId());

		return ChatRoomDto.RoomListResponse.from(
				roomWithPreviewMessages,
				totalUnread,
				rooms.hasNext()
		);
	}


	@Transactional
	public void leaveRoom(String roomId, String userId) {
		ChatRoomParticipant participant = chatRoomParticipantService.findByRoomIdAndUserId(roomId, userId);
		participant.deactivate();

		boolean allInactive = chatRoomRepository.existsActiveParticipantById(roomId);

		if (allInactive) {
			chatRoomRepository.deleteById(roomId);
			chatMessageService.deleteMessageByRoomId(roomId);
		}
	}

	public ChatRoom findByRoomIdAndUserId(String roomId, String userId) {
		return chatRoomRepository.findByIdAndParticipantsUserId(roomId, userId).orElseThrow(() ->
				new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CHAT_ROOM_NOT_FOUND));
	}

	private ChatRoom findOneToOneRoom(String senderId, String targetUserId) {
		List<ChatRoom> chatRooms = chatRoomRepository
				.findByRoomTypeAndParticipantsUserIdIn(ChatRoomType.ONE_TO_ONE, List.of(senderId, targetUserId));

		return chatRooms.stream()
				.filter(room -> room.getParticipants().size() == 2).findFirst().orElse(null);
	}
}
