package net.causw.adapter.persistence.repository.chat;

import net.causw.adapter.persistence.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
} 