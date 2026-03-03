package net.causw.app.main.domain.user.account.repository.user.query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<User> findAllActiveUsersByRoles(List<Role> roles) {
		QUser user = QUser.user;

		BooleanBuilder predicate = new BooleanBuilder();

		for (Role role : roles) {
			predicate.or(user.roles.contains(role));
		}
		predicate.and(user.state.eq(UserState.ACTIVE));

		return jpaQueryFactory.selectFrom(user)
			.where(predicate)
			.leftJoin(user.ceremonyNotificationSetting).fetchJoin()
			.leftJoin(user.locker).fetchJoin()
			.leftJoin(user.userProfileImage).fetchJoin()
			.leftJoin(user.userProfileImage.uuidFile).fetchJoin()
			.distinct()
			.fetch();
	}

	public Optional<User> findByIdWithRelations(String userId) {
		QUser user = QUser.user;
		User result = jpaQueryFactory.selectFrom(user)
			.where(user.id.eq(userId))
			.leftJoin(user.roles).fetchJoin()
			.leftJoin(user.ceremonyNotificationSetting).fetchJoin()
			.leftJoin(user.locker).fetchJoin()
			.leftJoin(user.userProfileImage).fetchJoin()
			.leftJoin(user.userProfileImage.uuidFile).fetchJoin()
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public Optional<User> findByEmail(String email) {
		QUser user = QUser.user;

		User result = jpaQueryFactory.selectFrom(user)
			.where(user.email.eq(email))
			.leftJoin(user.roles).fetchJoin()
			.leftJoin(user.ceremonyNotificationSetting).fetchJoin()
			.leftJoin(user.locker).fetchJoin()
			.leftJoin(user.userProfileImage).fetchJoin()
			.leftJoin(user.userProfileImage.uuidFile).fetchJoin()
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public List<User> findByIds(List<String> userIds) {
		QUser user = QUser.user;

		return jpaQueryFactory.selectFrom(user)
			.where(user.id.in(userIds))
			.leftJoin(user.roles).fetchJoin()
			.leftJoin(user.ceremonyNotificationSetting).fetchJoin()
			.leftJoin(user.locker).fetchJoin()
			.leftJoin(user.userProfileImage).fetchJoin()
			.leftJoin(user.userProfileImage.uuidFile).fetchJoin()
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

	public Page<User> findUserList(
		String keyword,
		UserState state,
		AcademicStatus academicStatus,
		Department department,
		Pageable pageable) {
		QUser user = QUser.user;

		BooleanBuilder where = new BooleanBuilder();

		if (keyword != null && !keyword.isBlank()) {
			where.and(
				user.name.containsIgnoreCase(keyword)
					.or(user.studentId.containsIgnoreCase(keyword)));
		}

		if (state != null) {
			where.and(user.state.eq(state));
		}

		if (academicStatus != null) {
			where.and(user.academicStatus.eq(academicStatus));
		}

		if (department != null) {
			where.and(user.department.eq(department));
		}

		List<User> content = jpaQueryFactory
			.selectFrom(user)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(user.createdAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	public Optional<User> findByIdNotDeleted(String userId) {
		QUser user = QUser.user;
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(user)
			.where(user.id.eq(userId))
			.where(notDeleted())
			.fetchOne());
	}

	private static BooleanExpression notDeleted() {
		QUser user = QUser.user;
		return user.deletedAt.isNull();
	}
}
