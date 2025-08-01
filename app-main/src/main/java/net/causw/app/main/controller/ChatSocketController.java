package net.causw.app.main.controller;

import java.security.Principal;

import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.chat.ChatRoomDto;
import net.causw.app.main.infrastructure.redis.chat.ChatRedisService;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.chat.ChatFacadeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;


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
