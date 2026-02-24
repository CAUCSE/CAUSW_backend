package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserBoardSubscribeWriter {

	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	public void upsertBoardSubscribe(User user, Board board, boolean subscribed) {
		userBoardSubscribeRepository.findByUserAndBoard(user, board)
			.ifPresentOrElse(
				existing -> existing.setIsSubscribed(subscribed),
				() -> userBoardSubscribeRepository.save(
					UserBoardSubscribe.of(user, board, subscribed))
			);
	}
}
