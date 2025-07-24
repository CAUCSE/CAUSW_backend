package net.causw.adapter.persistence.repository.chat;

import io.lettuce.core.dynamic.annotation.Param;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.adapter.persistence.chat.ChatRoomParticipant;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, String> {

    Optional<ChatRoomParticipant> findByChatRoomIdAndUserId(String chatRoomId, String userId);

    List<ChatRoomParticipant> findAllByChatRoomId(String chatRoomId);

    List<ChatRoomParticipant> findByChatRoomIdAndUserIdNotIn(String chatRoomId, Collection<String> userIds);
} 