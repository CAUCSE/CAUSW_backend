package net.causw.app.main.domain.user.account.repository.user.query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;

import com.querydsl.core.BooleanBuilder;
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

	public Optional<User> findByIdWithRoles(String userId) {
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
}
