package net.causw.application.chat;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.chat.ChatRoomParticipant;
import net.causw.adapter.persistence.repository.chat.ChatRoomParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public void pinRoom(String roomId, String userId) {
        ChatRoomParticipant participant = findByRoomIdAndUserId(roomId, userId);
        participant.pin();
        chatRoomParticipantRepository.save(participant);
    }

    public void unpinRoom(String roomId, String userId) {
        ChatRoomParticipant participant = findByRoomIdAndUserId(roomId, userId);
        participant.unpin();
        chatRoomParticipantRepository.save(participant);
    }

    public List<ChatRoomParticipant> getOtherParticipants(String roomId, String senderId) {
        return chatRoomParticipantRepository.findByChatRoomIdAndUserIdNotIn(roomId, List.of(senderId));
    }

    public ChatRoomParticipant findByRoomIdAndUserId(String roomId, String userId) {
        return chatRoomParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 참가자가 아닙니다"));
    }

    public List<ChatRoomParticipant> findAllByRoomId(String roomId) {
        return chatRoomParticipantRepository.findAllByChatRoomId(roomId);
    }

    public void updateLastReadAt(ChatRoomParticipant participant) {
        participant.updateLastReadAt();
        chatRoomParticipantRepository.save(participant);
    }
}
