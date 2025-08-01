package net.causw.app.main.service.chat;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;
import net.causw.app.main.domain.model.entity.chat.ChatRoom;
import net.causw.app.main.domain.model.entity.chat.ChatRoomParticipant;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.socket.SocketPayloadType;
import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.socket.SocketPayload;
import net.causw.app.main.infrastructure.redis.chat.ChatRedisService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatMessageSocketService {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatMessageService chatMessageService;
	private final ChatRoomParticipantService chatRoomParticipantService;
	private final ChatNotificationService chatNotificationService;
	private final ChatRedisService redisService;

	public ChatMessageDto.MessageResponse processIncomingMessage(
		ChatMessageDto.SendMessageCommand command,
		ChatRoom chatRoom,
		User sender
	) {
		chatRoomParticipantService.findByRoomIdAndUserId(command.getRoomId(), sender.getId());

		ChatMessage savedMessage = chatMessageService.saveMessage(command, sender.getId());
		ChatMessageDto.MessageResponse response = ChatMessageDto.MessageResponse.from(savedMessage,
			command.getUuidFiles());

		List<User> receivers = chatRoomParticipantService.getOtherParticipants(command.getRoomId(), sender.getId())
			.stream().map(ChatRoomParticipant::getUser).toList();

		messagingTemplate.convertAndSend(
			"/topic/room/" + command.getRoomId(),
			SocketPayload.of(SocketPayloadType.MESSAGE, response)
		);

		receivers.forEach(receiver -> {
			boolean isSubscribed = redisService.getSession(receiver.getId(), command.getRoomId()) != null;

			if (!isSubscribed) {
				redisService.incrementUnreadCount(receiver.getId(), command.getRoomId());
				chatNotificationService.sendByChatIsSubscribed(chatRoom, savedMessage.getContent(), sender, receiver);
			}
		});

		return response;
	}

	//    public void processReadReceipt(String roomId, String userId) {
	//        chatRoomParticipantService.updateLastReadAt(roomId, userId);
	//
	//        // 2. Redis에서 unreadCount 제거
	//        redisService.clearUnreadCount(userId, roomId);
	//
	//        // 3. 구독자에게 읽음 이벤트 전파
	//        messagingTemplate.convertAndSend(
	//                "/topic/room/" + roomId + "/read",
	//                SocketPayload.of(SocketPayloadType.READ_RECEIPT, new ReadReceiptDto(userId, roomId))
	//        );
	//    }
}
