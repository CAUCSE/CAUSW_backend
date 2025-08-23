package net.causw.app.main.service.board;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.repository.notification.UserBoardSubscribeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBoardSubscribeService {

	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	public boolean isBoardSubscribed(User user, Board board){

		return userBoardSubscribeRepository.findByUserAndBoard(user, board)
			.map(UserBoardSubscribe::getIsSubscribed)
			.orElse(false);
	}
}
