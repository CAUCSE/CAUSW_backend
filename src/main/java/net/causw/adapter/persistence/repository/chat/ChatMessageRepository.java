package net.causw.adapter.persistence.repository.chat;

import net.causw.adapter.persistence.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Optional<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId);

    List<ChatMessage> findByRoomIdAndTimestampBeforeOrderByTimestampDesc(
            String roomId,
            LocalDateTime timestamp,
            Pageable pageable
    );

    void deleteByRoomId(String roomId);
}