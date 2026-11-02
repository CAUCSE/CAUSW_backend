package net.causw.app.main.domain.notification.notification.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
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

	/**
	 * <p>
	 *     특정 게시판들에 대한 유저의 알림 구독 여부
	 * </p>
	 * <p>
	 *     알림 설정 row 가 없는 게시판은 {@code defaultSubscribed} 값을 따른다.
	 *     (소식 게시판: ON, 소통 게시판: OFF)
	 * </p>
	 *
	 * @param user 확인 대상 유저
	 * @param boards 확인 대상 게시판
	 * @param defaultSubscribed row가 없는 경우의 기본 구독 여부
	 * @return 알림 ON 인 boardId Set
	 */
	public Set<String> findSubscribedBoardIds(User user, List<Board> boards, boolean defaultSubscribed) {
		if (boards.isEmpty()) {
			return Set.of();
		}
		// boardId -> 명시적으로 저장된 구독 여부
		Map<String, Boolean> storedMap = userBoardSubscribeRepository.findByUserAndBoardIn(user, boards).stream()
			.collect(Collectors.toMap(
				subscribe -> subscribe.getBoard().getId(),
				UserBoardSubscribe::getIsSubscribed));

		return boards.stream()
			.map(Board::getId)
			.filter(id -> storedMap.getOrDefault(id, defaultSubscribed))
			.collect(Collectors.toSet());
	}

	public List<UserBoardSubscribe> findForNotification(Board board, Set<String> blockerUserIds) {
		return userBoardSubscribeRepository.findByBoardAndIsSubscribedTrueExcludingBlockerUsers(board, blockerUserIds);
	}

	/**
	 * 소식(공식 게시판) 글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>발송 조건: 소식 전체 알림 ON AND (채널 구독 ON OR 게시글 성격 알림 ON)
	 * <br>설정 row가 없으면 ON으로 간주합니다. 게시글 성격이 없으면 채널 구독 여부만 판단합니다.
	 *
	 * <p>권한 정책: 게시판 관리자는 학적·학과 제한 없이 알림 대상에 포함됩니다.
	 *
	 * @param boardId         알림을 발송할 게시판 ID
	 * @param newsCategoryKey 게시글 성격에 대응하는 설정 키 (미분류·기타 성격이면 null)
	 * @param boardConfig     게시판 설정 (읽기 범위·허용 학과 조건 추출에 사용)
	 * @param boardAdminIds   게시판 관리자 ID Set
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findNotificationTargets(
		String boardId,
		UserNotificationSettingKey newsCategoryKey,
		BoardConfig boardConfig,
		Set<String> boardAdminIds) {
		return userBoardSubscribeQueryRepository.findNotificationTargets(
			boardId,
			newsCategoryKey,
			boardConfig.getReadScope().getTargetAcademicStatuses(),
			boardConfig.getDepartments(),
			boardAdminIds);
	}

	/**
	 * 소통 게시판 새 글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: row가 없으면 구독하지 않은 상태(OFF)로 간주합니다.
	 * <p>권한 정책: 게시판 관리자는 학적·학과 제한 없이 알림 대상에 포함됩니다.
	 *
	 * @param boardId         알림을 발송할 게시판 ID
	 * @param boardConfig     게시판 설정 (읽기 범위·허용 학과 조건 추출에 사용)
	 * @param boardAdminIds   게시판 관리자 ID Set
	 * @param excludedUserIds 발송 대상에서 제외할 유저 ID Set
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findCommunityNotificationTargets(
		String boardId,
		BoardConfig boardConfig,
		Set<String> boardAdminIds,
		Set<String> excludedUserIds) {
		return userBoardSubscribeQueryRepository.findCommunityNotificationTargets(
			boardId,
			boardConfig.getReadScope().getTargetAcademicStatuses(),
			boardConfig.getDepartments(),
			boardAdminIds,
			excludedUserIds);
	}
}
