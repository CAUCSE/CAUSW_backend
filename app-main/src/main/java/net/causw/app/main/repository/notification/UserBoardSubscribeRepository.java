package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.user.User;

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
