package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBoardSubscribeRepository extends JpaRepository<UserBoardSubscribe, String> {
    Optional<UserBoardSubscribe> findByUser_IdAndBoard_Id(String userId, String boardId);

    List<UserBoardSubscribe> findByBoard_Id(String boardId);
}
