package net.causw.app.main.infrastructure.websocket;

import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

public interface WebSocketEventHandler {
	default void handleConnect(SessionConnectedEvent event) {
	}

	default void handleSubscribe(SessionSubscribeEvent event) {
	}

	default void handleUnsubscribe(SessionUnsubscribeEvent event) {
	}

	default void handleDisconnect(SessionDisconnectEvent event) {
	}

	default boolean isSubscribed(String sessionId, String destination) {
		return false;
	}
}
