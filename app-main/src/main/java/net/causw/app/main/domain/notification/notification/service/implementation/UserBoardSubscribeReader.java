package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeQueryRepository;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBoardSubscribeReader {

	private final UserBoardSubscribeRepository userBoardSubscribeRepository;
	private final UserBoardSubscribeQueryRepository userBoardSubscribeQueryRepository;

	public Set<String> findSubscribedBoardIds(User user, List<Board> boards) {
		return userBoardSubscribeRepository.findByUserAndBoardIn(user, boards).stream()
			.filter(s -> Boolean.TRUE.equals(s.getIsSubscribed()))
			.map(s -> s.getBoard().getId())
			.collect(Collectors.toSet());
	}

	public List<UserBoardSubscribe> findForNotification(Board board, Set<String> blockerUserIds) {
		return userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(board, blockerUserIds);
	}

	/**
	 * 공식 게시글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: {@code UserBoardSubscribe} row가 없으면 구독 상태(true)로 간주합니다.
	 * {@code isSubscribed = false}인 row가 명시적으로 존재하는 경우에만 알림 대상에서 제외됩니다.
	 *
	 * @param boardId   알림을 발송할 게시판 ID
	 * @param readScope 게시판 읽기 범위
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findNotificationTargets(String boardId, BoardReadScope readScope) {
		return userBoardSubscribeQueryRepository.findNotificationTargets(boardId,
			readScope.getTargetAcademicStatuses());
	}
}
