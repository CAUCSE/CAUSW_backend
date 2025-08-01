package net.causw.app.main.repository.chat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.chat.ChatRoomParticipant;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, String> {

	Optional<ChatRoomParticipant> findByChatRoomIdAndUserId(String chatRoomId, String userId);

	List<ChatRoomParticipant> findAllByChatRoomId(String chatRoomId);

	List<ChatRoomParticipant> findByChatRoomIdAndUserIdNotIn(String chatRoomId, Collection<String> userIds);
} 