package net.causw.app.main.domain.asset.locker.repository.query;

import java.time.LocalDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.entity.QLocker;
import net.causw.app.main.domain.asset.locker.entity.QLockerLocation;
import net.causw.app.main.domain.user.account.entity.user.QUser;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LockerQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<Locker> findLockers(
		String userKeyword,
		LockerName location,
		Boolean isActive,
		Boolean isOccupied,
		Boolean isExpired,
		Pageable pageable) {

		QLocker locker = QLocker.locker;
		QLockerLocation lockerLocation = QLockerLocation.lockerLocation;
		QUser user = QUser.user;

		BooleanBuilder predicate = getSearchCondition(userKeyword, location, isActive, isOccupied, isExpired, user,
			lockerLocation, locker);

		List<Locker> content = jpaQueryFactory
			.selectFrom(locker)
			.join(locker.location, lockerLocation).fetchJoin()
			.leftJoin(locker.user, user).fetchJoin()
			.where(predicate)
			.orderBy(lockerLocation.id.asc(), locker.lockerNumber.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(locker.count())
			.from(locker)
			.join(locker.location, lockerLocation)
			.leftJoin(locker.user, user)
			.where(predicate);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	/**
	 * 만료된 사물함 리스트 조회 (user join fetch)
	 * @param targetTime 조회 기준 시간 (현재 시간)
	 * @return 만료된 사물함 리스트
	 */
	public List<Locker> findAllExpiredLockersWithUser(LocalDateTime targetTime) {
		QLocker locker = QLocker.locker;

		return jpaQueryFactory
			.selectFrom(locker)
			.leftJoin(locker.user).fetchJoin()
			.where(locker.expireDate.before(targetTime))
			.fetch();
	}

	@NotNull
	private static BooleanBuilder getSearchCondition(String userKeyword, LockerName location, Boolean isActive,
		Boolean isOccupied, Boolean isExpired, QUser user, QLockerLocation lockerLocation, QLocker locker) {
		BooleanBuilder where = new BooleanBuilder();

		if (userKeyword != null && !userKeyword.isBlank()) {
			where.and(
				user.name.containsIgnoreCase(userKeyword)
					.or(user.email.containsIgnoreCase(userKeyword))
					.or(user.studentId.containsIgnoreCase(userKeyword)));
		}

		if (location != null) {
			where.and(lockerLocation.name.eq(location));
		}

		if (isActive != null) {
			where.and(locker.isActive.eq(isActive));
		}

		if (isOccupied != null) {
			if (isOccupied) {
				where.and(locker.user.isNotNull());
			} else {
				where.and(locker.user.isNull());
			}
		}

		if (isExpired != null) {
			if (isExpired) {
				where.and(locker.expireDate.isNotNull()
					.and(locker.expireDate.before(LocalDateTime.now())));
			} else {
				where.and(locker.expireDate.isNull()
					.or(locker.expireDate.goe(LocalDateTime.now())));
			}
		}
		return where;
	}
}
