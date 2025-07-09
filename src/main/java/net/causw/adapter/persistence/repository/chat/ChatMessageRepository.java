package net.causw.adapter.persistence.repository.chat;

import net.causw.adapter.persistence.chat.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId);
}