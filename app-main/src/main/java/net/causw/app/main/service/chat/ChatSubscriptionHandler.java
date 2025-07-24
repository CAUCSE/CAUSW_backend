package net.causw.app.main.service.chat;

import java.util.Set;

import net.causw.app.main.infrastructure.redis.chat.ChatRedisService;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import net.causw.application.common.WebSocketEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatSubscriptionHandler implements WebSocketEventHandler {

	private final ChatRedisService chatRedisService;

	@Override
	public void handleSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();
		String destination = accessor.getDestination();
		String userId = getUserId(accessor);

		if (destination != null && destination.startsWith("/topic/room/") && userId != null) {
			String roomId = extractRoomId(destination);

			chatRedisService.setSession(userId, roomId);
			chatRedisService.addSessionReverseMapping(sessionId, userId, roomId);
			log.info("[SUBSCRIBE] userId={} subscribed to roomId={} (sessionId={})", userId, roomId, sessionId);
		}
	}

	@Override
	public void handleDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		Set<String> mappings = chatRedisService.getSessionReverseMappings(sessionId);

		for (String mapping : mappings) {
			String[] parts = mapping.split(":");
			if (parts.length == 2) {
				String userId = parts[0];
				String roomId = parts[1];
				chatRedisService.deleteSession(userId, roomId);
				log.info("[DISCONNECT] cleaned session for userId={} roomId={} (sessionId={})", userId, roomId,
					sessionId);
			}
		}

		chatRedisService.deleteSessionReverse(sessionId);
		log.info("[DISCONNECT] sessionId={} disconnected", sessionId);
	}

	@Override
	public boolean isSubscribed(String sessionId, String destination) {
		if (destination == null || !destination.startsWith("/topic/room/")) {
			return false;
		}

		String roomId = extractRoomId(destination);
		Set<String> mappings = chatRedisService.getSessionReverseMappings(sessionId);
		return mappings.stream()
			.anyMatch(mapping -> mapping.equals(sessionUserRoomKey(sessionId, roomId)));
	}

	private String getUserId(StompHeaderAccessor accessor) {
		return accessor.getUser() != null ? accessor.getUser().getName() : null;
	}

	private String extractRoomId(String destination) {
		return destination.substring("/topic/room/".length());
	}

	private String sessionUserRoomKey(String userId, String roomId) {
		return userId + ":" + roomId;
	}
}