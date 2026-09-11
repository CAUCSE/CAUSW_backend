package net.causw.app.main.domain.notification.notification.repository;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.QUserBoardSubscribe;
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
	 * 공식 게시글 알림 발송 대상 유저 목록을 조회합니다.
	 *
	 * <p>기본 구독 정책: {@code UserBoardSubscribe} row가 존재하지 않으면 구독 상태(true)로 간주합니다.
	 * {@code isSubscribed = false}인 row가 명시적으로 존재하는 경우에만 알림 대상에서 제외됩니다.
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
	 *   <li>해당 게시판에 {@code isSubscribed = false}인 row가 존재하지 않는 사용자</li>
	 * </ol>
	 *
	 * @param boardId                알림을 발송할 게시판 ID
	 * @param targetAcademicStatuses 허용할 학적 상태 목록 (빈 리스트이면 모든 학적 허용)
	 * @param allowedDepartments     허용할 학과 Set (빈 Set이면 모든 학과 허용)
	 * @param boardAdminIds          게시판 관리자 ID Set (학적·학과 조건 우회)
	 * @return 알림 발송 대상 유저 목록
	 */
	public List<User> findNotificationTargets(
		String boardId,
		List<AcademicStatus> targetAcademicStatuses,
		Set<Department> allowedDepartments,
		Set<String> boardAdminIds) {

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
						ubs.isSubscribed.isFalse())
					.notExists())
			.fetch();
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
