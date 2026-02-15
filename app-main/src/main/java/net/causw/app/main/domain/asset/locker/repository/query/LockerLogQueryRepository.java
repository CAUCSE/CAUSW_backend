package net.causw.app.main.domain.asset.locker.repository.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.asset.locker.entity.QLockerLog;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LockerLogQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<LockerLog> findLockerLogList(
		String userKeyword,
		LockerLogAction action,
		LockerName lockerLocationName,
		Long lockerNumber,
		Pageable pageable) {

		QLockerLog lockerLog = QLockerLog.lockerLog;

		BooleanBuilder where = new BooleanBuilder();

		if (userKeyword != null && !userKeyword.isBlank()) {
			where.and(
				lockerLog.userName.containsIgnoreCase(userKeyword)
					.or(lockerLog.userEmail.containsIgnoreCase(userKeyword)));
		}

		if (action != null) {
			where.and(lockerLog.action.eq(action));
		}

		if (lockerLocationName != null) {
			where.and(lockerLog.lockerLocationName.eq(lockerLocationName.name()));
		}

		if (lockerNumber != null) {
			where.and(lockerLog.lockerNumber.eq(lockerNumber));
		}

		List<LockerLog> content = jpaQueryFactory
			.selectFrom(lockerLog)
			.where(where)
			.orderBy(lockerLog.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(lockerLog.count())
			.from(lockerLog)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}
}
