package net.causw.app.main.repository.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.chat.ChatMessage;

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