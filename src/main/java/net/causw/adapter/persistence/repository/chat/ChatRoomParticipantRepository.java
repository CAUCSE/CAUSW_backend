package net.causw.adapter.persistence.repository.chat;

import net.causw.adapter.persistence.chat.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, String> {
} 