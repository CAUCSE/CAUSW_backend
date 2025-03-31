package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.notification.UserCommentSubscribe;
import net.causw.adapter.persistence.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBoardSubscribeRepository extends JpaRepository<UserBoardSubscribe, String> {

    Optional<UserBoardSubscribe> findByUserAndBoard(User user, Board board);

    @EntityGraph(attributePaths = {"user"})
    List<UserBoardSubscribe> findByBoardAndIsSubscribedTrue(Board board);
}
