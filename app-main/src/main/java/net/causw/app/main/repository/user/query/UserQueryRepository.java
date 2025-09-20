package net.causw.app.main.repository.user.query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.user.QUser;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;

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
}
