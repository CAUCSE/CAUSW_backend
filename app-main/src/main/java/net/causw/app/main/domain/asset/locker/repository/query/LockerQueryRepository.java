package net.causw.app.main.domain.asset.locker.repository.query;

import java.util.List;

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

	public Page<Locker> findLockerList(
		LockerName location,
		Boolean isActive,
		Boolean isOccupied,
		Pageable pageable) {

		QLocker locker = QLocker.locker;
		QLockerLocation lockerLocation = QLockerLocation.lockerLocation;
		QUser user = QUser.user;

		BooleanBuilder where = new BooleanBuilder();

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

		List<Locker> content = jpaQueryFactory
			.selectFrom(locker)
			.join(locker.location, lockerLocation).fetchJoin()
			.leftJoin(locker.user, user).fetchJoin()
			.where(where)
			.orderBy(locker.lockerNumber.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(locker.count())
			.from(locker)
			.join(locker.location, lockerLocation)
			.leftJoin(locker.user, user)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}
}
