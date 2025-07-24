package net.causw.app.main.service.chat;

import java.util.List;

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
		newRoom.setRoomProfileImage(ChatRoomProfileImage.of(roomProfileImageFile));
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
		ChatMessageDto.SendMessageCommand command = ChatMessageDto.SendMessageCommand.of(room.getId(), content,
			messageType, messageFiles);
		ChatMessageDto.MessageResponse message = chatMessageSocketService.processIncomingMessage(command, room, sender);

		ChatRoomDto.RoomDetail room = ChatRoomDto.RoomDetail.builder().build();

		return ChatMessageDto.FirstMessageResponse.from(roomId, response);
	}

	public ChatRoomDto.RoomListResponse getChatRooms(User user, int pageNum, int pageSize) {
		Page<ChatRoom> rooms = chatRoomRepository.findActiveChatRoomsByParticipant(user,
			PageRequest.of(pageNum, pageSize));

		List<ChatRoomDto.RoomDetail> roomDetails = rooms.stream().map(room -> {
			int unreadCount = redisService.getUnreadCount(user.getId(), room.getId());
			ChatMessage lastMessage = chatMessageService.findByRoomIdOrderByTimestampDesc(room.getId());

			return ChatRoomDto.RoomDetail.builder()
				.roomId(room.getId())
				.roomName(room.getRoomName())
				.roomType(room.getRoomType().name())
				.roomProfileUrl("")
				.unreadCount(unreadCount)
				.isPinned(room.getParticipants().stream()
					.filter(p -> p.getUser().getId().equals(""))
					.findFirst()
					.map(p -> p.getPinnedAt() != null)
					.orElse(false))
				.lastMessage(lastMessage != null ? ChatRoomDto.LastMessageDto.from(lastMessage) : null)
				.lastActivityAt(room.getCreatedAt()) // 수정 필요시 변경
				.createdAt(room.getCreatedAt())
				.build();
		}).toList();

		int totalUnread = redisService.getTotalUnreadCount(user.getId());

		return ChatRoomDto.RoomListResponse.builder()
			.rooms(roomDetails)
			.totalUnreadCount(totalUnread)
			.hasNext(false) // 페이징 구현 시 수정
			.build();
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
