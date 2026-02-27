package net.causw.app.main.domain.notification.notification.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class UserBoardSubscribeWriter {

	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	public void upsertBoardSubscribe(User user, Board board, boolean subscribed) {
		var existing = userBoardSubscribeRepository.findByUserAndBoard(user, board);

		if (existing.isPresent()) {
			existing.get().setIsSubscribed(subscribed);
		}else{
			userBoardSubscribeRepository.save(
				UserBoardSubscribe.of(user, board, subscribed));
		}
	}
}
