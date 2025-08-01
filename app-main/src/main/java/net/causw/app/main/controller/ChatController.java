package net.causw.app.main.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.dto.chat.chat.ChatMessageDto;
import net.causw.app.main.dto.chat.chat.ChatRoomDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.chat.ChatFacadeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

	private final ChatFacadeService chatFacadeService;

	// 첫 메시지 전송 및 채팅방 생성
	@PostMapping(value = "/messages/one-to-one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ChatMessageDto.FirstMessageResponse> sendFirstOneToOneMessage(
		@RequestPart ChatMessageDto.FirstOneToOneMessageRequest request,
		@RequestPart(required = false) List<MultipartFile> messageFiles,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return ResponseEntity.ok(
			chatFacadeService.sendFirstOneToOneMessage(request, messageFiles, userDetails.getUser()));
	}

	@PostMapping(value = "/messages/group", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ChatMessageDto.FirstMessageResponse> sendFirstGroupMessage(
		@RequestPart ChatMessageDto.FirstGroupMessageRequest request,
		@RequestPart MultipartFile roomProfileImage,
		@RequestPart(required = false) List<MultipartFile> messageFiles,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		ChatMessageDto.FirstMessageResponse response =
			chatFacadeService.sendFirstGroupMessage(request, roomProfileImage, messageFiles, userDetails.getUser());

		return ResponseEntity.ok(response);
	}

	// 채팅방 목록 조회
	@GetMapping("/rooms")
	public ResponseEntity<ChatRoomDto.RoomListResponse> getRooms(
		@RequestParam(defaultValue = "0") int pageNum,
		@RequestParam(defaultValue = "20") int pageSize,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(chatFacadeService.getChatRooms(userDetails.getUser(), pageNum, pageSize));
	}

	// 채팅방 메시지 및 참여자 정보 조회
	@GetMapping("/rooms/{roomId}/messages")
	public ResponseEntity<ChatMessageDto.MessagePageResponse> getMessages(
		@PathVariable String roomId,
		@RequestParam(required = false) LocalDateTime beforeTimestamp,
		@RequestParam(defaultValue = "20") int limit,
		Principal principal) {
		return ResponseEntity.ok(
			chatFacadeService.getMessagesWithParticipants(roomId, beforeTimestamp, limit, principal.getName()));
	}

	// 채팅방 고정
	@PostMapping("/rooms/{roomId}/pin")
	public ResponseEntity<Void> pinRoom(@PathVariable String roomId, Principal principal) {
		chatFacadeService.pinRoom(roomId, principal.getName());
		return ResponseEntity.ok().build();
	}

	// 채팅방 고정 해제
	@DeleteMapping("/rooms/{roomId}/pin")
	public ResponseEntity<Void> unpinRoom(@PathVariable String roomId, Principal principal) {
		chatFacadeService.unpinRoom(roomId, principal.getName());
		return ResponseEntity.ok().build();
	}

	// 채팅방 나가기
	@DeleteMapping("/rooms/{roomId}")
	public ResponseEntity<Void> leaveRoom(@PathVariable String roomId, Principal principal) {
		chatFacadeService.leaveRoom(roomId, principal.getName());
		return ResponseEntity.ok().build();
	}

	// 메시지 삭제
	@DeleteMapping("/message/{messageId}")
	public ResponseEntity<Void> deleteMessage(
		@PathVariable String messageId,
		Principal principal) {
		chatFacadeService.deleteMessage(messageId, principal.getName());
		return ResponseEntity.ok().build();
	}

	// 메시지 보내기
	@PostMapping(value = "message/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ChatMessageDto.MessageResponse> sendMessageWithFiles(
		@RequestPart @Valid ChatMessageDto.SendMessageRequest request,
		@RequestPart(required = false) List<MultipartFile> messageFiles,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(chatFacadeService.sendMessage(request, messageFiles, userDetails.getUser()));
	}
}