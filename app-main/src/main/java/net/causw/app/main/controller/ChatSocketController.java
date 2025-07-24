package net.causw.adapter.web;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import net.causw.application.chat.ChatFacadeService;
import net.causw.application.dto.chat.ChatMessageDto;
import net.causw.application.dto.chat.ChatRoomDto;
import net.causw.application.redis.chat.ChatRedisService;
import net.causw.config.security.userdetails.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatSocketController {

	private final ChatFacadeService chatFacadeService;
	private final ChatRedisService chatRedisService;

	@MessageMapping("/chat/message")
	public void handleChatMessage(ChatMessageDto.SendMessageRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		chatFacadeService.sendMessage(request, userDetails.getUser());
	}

	@MessageMapping("/chat/room/heartbeat")
	public void heartbeatSession(ChatRoomDto.RoomIdRequest request, Principal principal) {
		chatRedisService.setSession(principal.getName(), request.getRoomId());
	}

	@MessageMapping("/chat/room/leave")
	public void leaveRoom(ChatRoomDto.RoomIdRequest request, Principal principal) {
		chatRedisService.deleteSession(principal.getName(), request.getRoomId());
	}
}
