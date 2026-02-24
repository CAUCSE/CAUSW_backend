package net.causw.app.main.domain.notification.notification.service.v2.implementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBoardSubscribeReader {

	private final UserBoardSubscribeRepository userBoardSubscribeRepository;

	public Set<String> findSubscribedBoardIds(User user, List<Board> boards) {
		return userBoardSubscribeRepository.findByUserAndBoardIn(user, boards).stream()
			.filter(s -> Boolean.TRUE.equals(s.getIsSubscribed()))
			.map(s -> s.getBoard().getId())
			.collect(Collectors.toSet());
	}
}
