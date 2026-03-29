package net.causw.app.main.domain.notification.notification.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.QUserBoardSubscribe;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserBoardSubscribeQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 공식 게시글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: {@code UserBoardSubscribe} row가 존재하지 않으면 구독 상태(true)로 간주합니다.
	 * {@code isSubscribed = false}인 row가 명시적으로 존재하는 경우에만 알림 대상에서 제외됩니다.
	 *
	 * <p>발송 대상 조건:
	 * <ol>
	 *   <li>사용자 상태가 {@code ACTIVE}</li>
	 *   <li>삭제되지 않은 사용자 ({@code deletedAt IS NULL})</li>
	 *   <li>{@code targetAcademicStatuses}에 포함된 학적 상태 (빈 리스트이면 모든 학적 허용)</li>
	 *   <li>해당 게시판에 {@code isSubscribed = false}인 row가 존재하지 않는 사용자</li>
	 * </ol>
	 *
	 * @param boardId               알림을 발송할 게시판 ID
	 * @param targetAcademicStatuses 허용할 학적 상태 목록 ({@link net.causw.app.main.domain.community.board.entity.BoardReadScope#getTargetAcademicStatuses()} 반환값)
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findNotificationTargets(String boardId, List<AcademicStatus> targetAcademicStatuses) {
		QUser user = QUser.user;
		QUserBoardSubscribe ubs = QUserBoardSubscribe.userBoardSubscribe;

		// 해당 게시판에 isSubscribed=false로 명시적 구독 거부한 유저 ID 목록 조회
		List<String> unsubscribedIds = jpaQueryFactory
			.select(ubs.user.id)
			.from(ubs)
			.where(
				ubs.board.id.eq(boardId),
				ubs.isSubscribed.isFalse())
			.fetch();

		// ACTIVE + 미삭제 + 학적 조건을 만족하며 구독 거부하지 않은 유저 조회
		return jpaQueryFactory
			.selectFrom(user)
			.where(
				user.state.eq(UserState.ACTIVE),
				user.deletedAt.isNull(),
				academicStatusCondition(user, targetAcademicStatuses),
				unsubscribedIds.isEmpty() ? null : user.id.notIn(unsubscribedIds))
			.fetch();
	}

	/**
	 * 학적 상태 필터 조건. 빈 리스트이면 모든 학적 허용(BOTH).
	 */
	private BooleanExpression academicStatusCondition(QUser user, List<AcademicStatus> targetAcademicStatuses) {
		if (targetAcademicStatuses.isEmpty()) {
			return null;
		}
		return user.academicStatus.in(targetAcademicStatuses);
	}
}
