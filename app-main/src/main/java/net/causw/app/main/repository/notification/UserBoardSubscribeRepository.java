package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserBoardSubscribeRepository extends JpaRepository<UserBoardSubscribe, String> {

    Optional<UserBoardSubscribe> findByUserAndBoard(User user, Board board);

    @EntityGraph(attributePaths = {"user"})
    List<UserBoardSubscribe> findByBoardAndIsSubscribedTrue(Board board);

    @EntityGraph(attributePaths = {"user"})
    @Query(
        """
        SELECT ubs FROM UserBoardSubscribe ubs
        WHERE ubs.board = :board AND ubs.isSubscribed = true
        AND (:#{#blockerUserIds.size()} = 0 OR ubs.user.id NOT IN :blockerUserIds)
        """
    )
    List<UserBoardSubscribe> findByBoardAndIsSubscribedTrueExcludingBlockerUsers(
        @Param("board") Board board,
        @Param("blockerUserIds") Set<String> blockerUserIds);

    @EntityGraph(attributePaths = {"board"})
    List<UserBoardSubscribe> findByUserAndIsSubscribedTrue(User user);

    List<UserBoardSubscribe> findByUserAndBoardIn(User user, List<Board> boards);

    void deleteAllByUserAndBoard_IsAlumniFalse(User user);
}
