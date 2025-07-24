package net.causw.config.batch.listener;

import lombok.RequiredArgsConstructor;

import net.causw.application.common.WebSocketEventHandler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.List;

@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

	private final List<WebSocketEventHandler> handlers;

	@EventListener
	public void handleConnect(SessionConnectedEvent event) {
		handlers.forEach(handler -> handler.handleConnect(event));
	}

	@EventListener
	public void handleSubscribe(SessionSubscribeEvent event) {
		handlers.forEach(h -> h.handleSubscribe(event));
	}

	@EventListener
	public void handleUnsubscribe(SessionUnsubscribeEvent event) {
		handlers.forEach(h -> h.handleUnsubscribe(event));
	}

	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		handlers.forEach(h -> h.handleDisconnect(event));
	}
}