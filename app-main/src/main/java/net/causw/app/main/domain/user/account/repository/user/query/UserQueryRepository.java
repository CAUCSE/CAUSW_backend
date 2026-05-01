package net.causw.app.main.domain.user.account.repository.user.query;

import static net.causw.app.main.domain.user.account.entity.user.QUser.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.DeletedUserSortType;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserSortType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.DeletedUserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	private static BooleanExpression notDeleted() {
		QUser user = QUser.user;
		return user.deletedAt.isNull();
	}

	public List<User> findAllActiveUsersByRoles(List<Role> roles) {
		QUser user = QUser.user;

		BooleanBuilder predicate = new BooleanBuilder();

		for (Role role : roles) {
			predicate.or(user.roles.contains(role));
		}
		predicate.and(user.state.eq(UserState.ACTIVE));

		return jpaQueryFactory.selectFrom(user)
			.where(predicate)
			.distinct()
			.fetch();
	}

	public Optional<User> findByIdWithRelations(String userId) {
		QUser user = QUser.user;
		User result = jpaQueryFactory.selectFrom(user)
			.where(user.id.eq(userId))
			.leftJoin(user.roles).fetchJoin()
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public Optional<User> findByEmail(String email) {
		QUser user = QUser.user;

		User result = jpaQueryFactory.selectFrom(user)
			.where(user.email.eq(email))
			.leftJoin(user.roles).fetchJoin()
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public List<User> findByIds(List<String> userIds) {
		QUser user = QUser.user;

		return jpaQueryFactory.selectFrom(user)
			.where(user.id.in(userIds))
			.leftJoin(user.roles).fetchJoin()
			.distinct()
			.fetch();
	}

	public List<User> searchByCondition(UserQueryCondition condition) {
		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();

		if (condition.userState() != null) {
			predicate.and(user.state.eq(condition.userState()));
		}
		if (condition.userRole() != null) {
			predicate.and(user.roles.contains(condition.userRole()));
		}
		if (condition.keyword() != null && !condition.keyword().isBlank()) {
			String keyword = condition.keyword().trim();
			predicate.and(
				user.email.containsIgnoreCase(keyword)
					.or(user.name.containsIgnoreCase(keyword)));
		}

		return jpaQueryFactory.selectFrom(user)
			.where(predicate)
			.distinct()
			.fetch();
	}

	/**
	 * 관리자 유저 목록 조회 — QueryDSL Projection으로 DTO 직접 반환.
	 * 삭제 회원 제외(notDeleted), states 미지정 시 ACTIVE만 조회.
	 */
	public Page<UserListQueryResult> findUserList(UserListCondition condition, Pageable pageable) {
		List<UserState> states = (condition.states() == null || condition.states().isEmpty())
			? List.of(UserState.ACTIVE)
			: condition.states();

		BooleanBuilder where = new BooleanBuilder()
			.and(notDeleted())
			.and(userListKeywordCondition(condition.keyword()))
			.and(userStatesCondition(states))
			.and(academicStatusCondition(condition.academicStatus()))
			.and(departmentCondition(condition.department()))
			.and(admissionYearBetween(condition.admissionYearFrom(), condition.admissionYearTo()));

		List<UserListQueryResult> content = jpaQueryFactory
			.select(toUserListQueryResult(user))
			.from(user)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(resolveUserListOrder(condition.sortBy()))
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	/**
	 * 삭제(탈퇴) 회원 전용 목록 조회 — deletedAt이 null이 아닌 유저만 반환.
	 */
	public Page<DeletedUserListQueryResult> findDeletedUserList(
		DeletedUserQueryCondition condition,
		Pageable pageable) {

		BooleanBuilder where = new BooleanBuilder()
			.and(user.deletedAt.isNotNull())
			.and(adminKeywordCondition(condition.keyword()))
			.and(departmentCondition(condition.department()))
			.and(admissionYearBetween(condition.admissionYearFrom(), condition.admissionYearTo()))
			.and(academicStatusCondition(condition.academicStatus()));

		List<DeletedUserListQueryResult> content = jpaQueryFactory
			.select(toDeletedUserListQueryResult(user))
			.from(user)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(resolveDeletedUserOrder(condition.sortBy()))
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	public Page<User> findReportedUserList(
		String keyword,
		UserState state,
		AcademicStatus academicStatus,
		Pageable pageable) {
		QUser user = QUser.user;

		BooleanBuilder where = new BooleanBuilder()
			.and(notDeleted())
			.and(nameOrStudentIdKeywordCondition(keyword))
			.and(userStateCondition(state))
			.and(academicStatusCondition(academicStatus))
			.and(reportedUserCondition());

		List<User> contentList = jpaQueryFactory
			.selectFrom(user)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(user.reportCount.desc(), user.createdAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(where);

		return PageableExecutionUtils.getPage(contentList, pageable, countQuery::fetchOne);
	}

	public Optional<User> findByIdNotDeleted(String userId) {
		QUser user = QUser.user;
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(user)
			.where(user.id.eq(userId))
			.where(notDeleted())
			.fetchOne());
	}

	/**
	 * 입학년도에 해당하는 활성 유저 목록 조회
	 * @param admissionYears 조회할 입학년도 목록
	 * @return 해당 입학년도에 해당하는 활성 유저 목록
	 */
	public List<User> findByAdmissionYearIn(Collection<Integer> admissionYears) {
		QUser user = QUser.user;
		return jpaQueryFactory.selectFrom(user)
			.where(user.admissionYear.in(admissionYears))
			.where(user.state.eq(UserState.ACTIVE))
			.where(notDeleted())
			.fetch();
	}

	/**
	 * 모든 활성 유저 목록 조회
	 * @return 활성 유저 목록
	 */
	public List<User> findAllActive() {
		QUser user = QUser.user;
		return jpaQueryFactory.selectFrom(user)
			.where(user.state.eq(UserState.ACTIVE))
			.where(notDeleted())
			.fetch();
	}

	/**
	 * 특정 학적 상태에 해당하는 관리자 유저 목록 조회
	 * @param academicStatus 조회할 학적 상태
	 * @return 해당 학적 상태에 해당하는 관리자 유저 목록
	 */
	public List<User> findAdminsByAcademicStatus(AcademicStatus academicStatus) {
		return jpaQueryFactory.selectFrom(user)
			.where(user.roles.contains(Role.ADMIN))
			.where(user.academicStatus.eq(academicStatus))
			.where(notDeleted())
			.fetch();
	}

	public Long countTotalUsers() {
		return jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(user.state.eq(UserState.ACTIVE))
			.where(notDeleted())
			.fetchOne();
	}

	// ─── 정렬 헬퍼 ──────────────────────────────────────────────────────────────

	private OrderSpecifier<?> resolveUserListOrder(UserSortType sortBy) {
		if (sortBy == null) {
			return user.createdAt.desc();
		}
		return switch (sortBy) {
			case CREATED_AT_ASC -> user.createdAt.asc();
			case NAME_ASC -> user.name.asc();
			case NAME_DESC -> user.name.desc();
			case STUDENT_ID_ASC -> user.studentId.asc();
			default -> user.createdAt.desc();
		};
	}

	private OrderSpecifier<?> resolveDeletedUserOrder(DeletedUserSortType sortBy) {
		if (sortBy == null) {
			return user.deletedAt.desc();
		}
		return switch (sortBy) {
			case DELETED_AT_ASC -> user.deletedAt.asc();
			case NAME_ASC -> user.name.asc();
			default -> user.deletedAt.desc();
		};
	}

	// ─── 조건 헬퍼 ──────────────────────────────────────────────────────────────

	/** 이메일·이름·학번 OR like 검색 (관리자 유저 목록, 삭제 회원 검색용) */
	private BooleanExpression userListKeywordCondition(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		String k = keyword.trim();
		return user.email.containsIgnoreCase(k)
			.or(user.name.containsIgnoreCase(k))
			.or(user.studentId.containsIgnoreCase(k));
	}

	/** 이메일·이름·학번 OR like 검색 (삭제 회원 전용) */
	private BooleanExpression adminKeywordCondition(String keyword) {
		return userListKeywordCondition(keyword);
	}

	/** 이름·학번 OR like 검색 (신고 유저 목록 등 기존 용도) */
	private BooleanExpression nameOrStudentIdKeywordCondition(String keyword) {
		QUser user = QUser.user;
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return user.name.containsIgnoreCase(keyword)
			.or(user.studentId.containsIgnoreCase(keyword));
	}

	private BooleanExpression userStatesCondition(List<UserState> states) {
		return (states == null || states.isEmpty()) ? null : user.state.in(states);
	}

	private BooleanExpression userStateCondition(UserState state) {
		QUser user = QUser.user;
		return state == null ? null : user.state.eq(state);
	}

	private BooleanExpression academicStatusCondition(AcademicStatus academicStatus) {
		QUser user = QUser.user;
		return academicStatus == null ? null : user.academicStatus.eq(academicStatus);
	}

	private BooleanExpression departmentCondition(Department department) {
		QUser user = QUser.user;
		return department == null ? null : user.department.eq(department);
	}

	private BooleanExpression admissionYearBetween(Integer from, Integer to) {
		if (from == null && to == null) {
			return null;
		}
		if (from == null) {
			return user.admissionYear.loe(to);
		}
		if (to == null) {
			return user.admissionYear.goe(from);
		}
		return user.admissionYear.between(from, to);
	}

	private BooleanExpression reportedUserCondition() {
		QUser user = QUser.user;
		return user.reportCount.gt(0);
	}

	// ─── Projection 팩토리 ──────────────────────────────────────────────────────

	private static QUserListQueryResult toUserListQueryResult(QUser user) {
		return new QUserListQueryResult(
			user.id,
			user.name,
			user.email,
			user.studentId,
			user.admissionYear,
			user.department,
			user.state,
			user.academicStatus,
			user.createdAt);
	}

	private static QDeletedUserListQueryResult toDeletedUserListQueryResult(QUser user) {
		return new QDeletedUserListQueryResult(
			user.id,
			user.name,
			user.email,
			user.studentId,
			user.admissionYear,
			user.department,
			user.state,
			user.academicStatus,
			user.deletedAt,
			user.rejectionOrDropReason);
	}
}
