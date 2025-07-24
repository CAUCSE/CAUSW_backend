package net.causw.adapter.persistence.repository.chat;

import io.lettuce.core.dynamic.annotation.Param;
import net.causw.adapter.persistence.chat.ChatRoom;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.chat.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("""
    SELECT cr FROM ChatRoom cr
    JOIN cr.participants p
    WHERE p.user = :user AND p.isActive = true
    ORDER BY cr.createdAt DESC
    """)
    Page<ChatRoom> findActiveChatRoomsByParticipant(
            @Param("user") User user,
            Pageable pageable
    );

    List<ChatRoom> findByRoomTypeAndParticipantsUserIdIn(ChatRoomType chatRoomType, Collection<String> userIds);

    Optional<ChatRoom> findByIdAndParticipantsUserId(String id, String userId);

    boolean existsActiveParticipantById(String id);
} 