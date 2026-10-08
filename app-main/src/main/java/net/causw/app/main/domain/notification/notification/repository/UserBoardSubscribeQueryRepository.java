package net.causw.app.main.domain.notification.notification.repository;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.QUserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.entity.QUserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserBoardSubscribeQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 소식(공식 게시판) 글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: 설정 row가 없으면 켜짐(true)으로 간주합니다.
	 * {@code false}인 row가 명시적으로 존재하는 경우에만 꺼짐으로 판단합니다.
	 *
	 * <p>발송 대상 조건:
	 * <ol>
	 *   <li>사용자 상태가 {@code ACTIVE}</li>
	 *   <li>아래 중 하나를 만족:
	 *     <ul>
	 *       <li>{@code boardAdminIds}에 포함된 게시판 관리자 (학적·학과 조건 우회)</li>
	 *       <li>그 외: {@code targetAcademicStatuses}에 포함된 학적 상태 AND {@code allowedDepartments}에 포함된 학과</li>
	 *     </ul>
	 *   </li>
	 *   <li>소식 전체 알림({@link UserNotificationSettingKey#NEWS_NOTIFICATION_ENABLED})이 꺼져 있지 않은 사용자</li>
	 *   <li>해당 게시판(채널) 구독이 꺼져 있지 않거나, 게시글 성격 알림이 꺼져 있지 않은 사용자 (OR)
	 *     <br>게시글 성격이 없는 경우({@code newsCategoryKey == null}) 채널 구독 여부만 판단합니다.</li>
	 * </ol>
	 *
	 * @param boardId                알림을 발송할 게시판 ID
	 * @param newsCategoryKey        게시글 성격에 대응하는 설정 키 (미분류·기타 성격이면 null)
	 * @param targetAcademicStatuses 허용할 학적 상태 목록 (빈 리스트이면 모든 학적 허용)
	 * @param allowedDepartments     허용할 학과 Set (빈 Set이면 모든 학과 허용)
	 * @param boardAdminIds          게시판 관리자 ID Set (학적·학과 조건 우회)
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findNotificationTargets(
		String boardId,
		UserNotificationSettingKey newsCategoryKey,
		List<AcademicStatus> targetAcademicStatuses,
		Set<Department> allowedDepartments,
		Set<String> boardAdminIds) {

		QUser user = QUser.user;

		BooleanExpression channelSubscribed = boardSubscribeDisabledExists(user, boardId).not();
		BooleanExpression channelOrCategory = newsCategoryKey == null
			? channelSubscribed
			: channelSubscribed.or(settingDisabledExists(user, newsCategoryKey).not());

		return jpaQueryFactory
			.selectFrom(user)
			.where(
				user.state.eq(UserState.ACTIVE),
				accessCondition(user, targetAcademicStatuses, allowedDepartments, boardAdminIds),
				settingDisabledExists(user, UserNotificationSettingKey.NEWS_NOTIFICATION_ENABLED).not(),
				channelOrCategory)
			.fetch();
	}

	/**
	 * 소통 게시판 새 글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: 소통 게시판은 row가 없으면 구독하지 않은 상태(false)로 간주합니다.
	 * {@code isSubscribed = true}인 row가 명시적으로 존재하는 사용자만 대상입니다.
	 *
	 * <p>발송 대상 조건:
	 * <ol>
	 *   <li>사용자 상태가 {@code ACTIVE}</li>
	 *   <li>게시판 관리자이거나 학적·학과 조건을 만족 ({@link #findNotificationTargets}와 동일)</li>
	 *   <li>해당 게시판에 {@code isSubscribed = true}인 row가 존재하는 사용자</li>
	 *   <li>{@code excludedUserIds}에 포함되지 않은 사용자 (작성자, 작성자를 차단한 사용자 등)</li>
	 * </ol>
	 *
	 * @param boardId                알림을 발송할 게시판 ID
	 * @param targetAcademicStatuses 허용할 학적 상태 목록 (빈 리스트이면 모든 학적 허용)
	 * @param allowedDepartments     허용할 학과 Set (빈 Set이면 모든 학과 허용)
	 * @param boardAdminIds          게시판 관리자 ID Set (학적·학과 조건 우회)
	 * @param excludedUserIds        발송 대상에서 제외할 유저 ID Set
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findCommunityNotificationTargets(
		String boardId,
		List<AcademicStatus> targetAcademicStatuses,
		Set<Department> allowedDepartments,
		Set<String> boardAdminIds,
		Set<String> excludedUserIds) {

		QUser user = QUser.user;
		QUserBoardSubscribe ubs = QUserBoardSubscribe.userBoardSubscribe;

		return jpaQueryFactory
			.selectFrom(user)
			.where(
				user.state.eq(UserState.ACTIVE),
				accessCondition(user, targetAcademicStatuses, allowedDepartments, boardAdminIds),
				JPAExpressions.selectOne()
					.from(ubs)
					.where(
						ubs.user.id.eq(user.id),
						ubs.board.id.eq(boardId),
						ubs.isSubscribed.isTrue())
					.exists(),
				excludedUserIds.isEmpty() ? null : user.id.notIn(excludedUserIds))
			.fetch();
	}

	/**
	 * 해당 게시판 구독을 명시적으로 끈({@code isSubscribed = false}) row가 존재하는지.
	 */
	private BooleanExpression boardSubscribeDisabledExists(QUser user, String boardId) {
		QUserBoardSubscribe ubs = QUserBoardSubscribe.userBoardSubscribe;
		return JPAExpressions.selectOne()
			.from(ubs)
			.where(
				ubs.user.id.eq(user.id),
				ubs.board.id.eq(boardId),
				ubs.isSubscribed.isFalse())
			.exists();
	}

	/**
	 * 해당 알림 설정 키를 명시적으로 끈({@code enabled = false}) row가 존재하는지.
	 */
	private BooleanExpression settingDisabledExists(QUser user, UserNotificationSettingKey key) {
		QUserNotificationSetting setting = QUserNotificationSetting.userNotificationSetting;
		return JPAExpressions.selectOne()
			.from(setting)
			.where(
				setting.userId.eq(user.id),
				setting.settingKey.eq(key),
				setting.enabled.isFalse())
			.exists();
	}

	/**
	 * 게시판 관리자 우회를 포함한 접근 조건을 구성합니다.
	 *
	 * <p>학적·학과 제한이 없는 경우({@code commonCondition == null})
	 * 모든 ACTIVE 유저가 이미 대상이므로 OR 분기 없이 {@code null}을 반환합니다.
	 * QueryDSL {@code where(null)}은 해당 조건을 무시하므로 안전합니다.
	 */
	private BooleanExpression accessCondition(
		QUser user,
		List<AcademicStatus> targetAcademicStatuses,
		Set<Department> allowedDepartments,
		Set<String> boardAdminIds) {

		BooleanExpression commonCondition = commonUserCondition(user, targetAcademicStatuses, allowedDepartments);
		if (commonCondition == null) {
			return null;
		}

		BooleanExpression isAdmin = boardAdminIds.isEmpty() ? null : user.id.in(boardAdminIds);
		return isAdmin != null ? isAdmin.or(commonCondition) : commonCondition;
	}

	/**
	 * 학적·학과 조건을 AND로 결합합니다. 둘 다 제한 없으면 {@code null}을 반환합니다.
	 */
	private BooleanExpression commonUserCondition(
		QUser user,
		List<AcademicStatus> targetAcademicStatuses,
		Set<Department> allowedDepartments) {

		BooleanExpression academic = academicStatusCondition(user, targetAcademicStatuses);
		BooleanExpression dept = departmentCondition(user, allowedDepartments);
		if (academic == null && dept == null) {
			return null;
		}
		if (academic == null) {
			return dept;
		}
		if (dept == null) {
			return academic;
		}
		return academic.and(dept);
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

	/**
	 * 학과 필터 조건. 빈 Set이면 모든 학과 허용.
	 * department가 null인 유저는 학과 미확정 레거시 유저로 간주하여 제한 없이 포함한다.
	 * 이 정책은 CommunityPermissionPolicy.isDepartmentAllowed()와 동일하게 유지되어야 한다.
	 * TODO: department 백필 완료 후 isNull() 조건 제거
	 */
	private BooleanExpression departmentCondition(QUser user, Set<Department> allowedDepartments) {
		if (allowedDepartments.isEmpty()) {
			return null;
		}
		return user.department.isNull().or(user.department.in(allowedDepartments));
	}
}
