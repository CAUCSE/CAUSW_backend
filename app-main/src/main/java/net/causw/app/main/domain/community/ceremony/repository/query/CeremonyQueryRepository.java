package net.causw.app.main.domain.community.ceremony.repository.query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.entity.QCeremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CeremonyQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<Ceremony> findOngoingOrderByStartedAtDesc(
		String type, LocalDate nowDate, LocalTime nowTime, Pageable pageable) {

		QCeremony ceremony = QCeremony.ceremony;
		BooleanExpression condition = baseCondition(type, ceremony)
			.and(createOngoingCondition(nowDate, nowTime, ceremony));

		List<Ceremony> content = jpaQueryFactory
			.selectFrom(ceremony)
			.where(condition)
			.orderBy(ceremony.startDate.desc(), ceremony.startTime.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(ceremony.count())
			.from(ceremony)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	public Page<Ceremony> findUpcomingOrderByStartedAtAsc(
		String type, LocalDate nowDate, LocalTime nowTime, Pageable pageable) {

		QCeremony ceremony = QCeremony.ceremony;

		BooleanExpression condition = baseCondition(type, ceremony)
			.and(createUpcomingCondition(nowDate, nowTime, ceremony));

		List<Ceremony> content = jpaQueryFactory
			.selectFrom(ceremony)
			.where(condition)
			.orderBy(ceremony.startDate.asc(), ceremony.startTime.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(ceremony.count())
			.from(ceremony)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	public Page<Ceremony> findPastOrderByStartedAtDesc(
		String type, LocalDate nowDate, LocalTime nowTime, Pageable pageable) {

		QCeremony ceremony = QCeremony.ceremony;
		BooleanExpression condition = baseCondition(type, ceremony)
			.and(createPastCondition(nowDate, nowTime, ceremony));

		List<Ceremony> content = jpaQueryFactory
			.selectFrom(ceremony)
			.where(condition)
			.orderBy(ceremony.startDate.desc(), ceremony.startTime.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(ceremony.count())
			.from(ceremony)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	// 관리자 경조사 목록 조회: fromDate~toDate 범위 내 경조사 시작일 필터링, state별 필터링 (각 조건은 nullable하여 동적 적용)
	public Page<Ceremony> findAllForAdmin(
		LocalDate fromDate, LocalDate toDate, CeremonyState state, Pageable pageable) {

		QCeremony ceremony = QCeremony.ceremony;
		BooleanBuilder condition = new BooleanBuilder();

		if (state != null) {
			condition.and(ceremony.ceremonyState.eq(state));
		}
		if (fromDate != null) {
			condition.and(ceremony.startDate.goe(fromDate));
		}
		if (toDate != null) {
			condition.and(ceremony.startDate.loe(toDate));
		}

		List<Ceremony> content = jpaQueryFactory
			.selectFrom(ceremony)
			.join(ceremony.user).fetchJoin()
			.where(condition)
			.orderBy(ceremony.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(ceremony.count())
			.from(ceremony)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression baseCondition(String type, QCeremony ceremony) {
		BooleanExpression condition = ceremony.ceremonyState.eq(CeremonyState.ACCEPT);
		String parsedType = CeremonyType.parseTypeOrNull(type);

		if (parsedType != null) {
			CeremonyType ceremonyType = CeremonyType.fromString(parsedType);
			condition = condition.and(ceremony.ceremonyType.eq(ceremonyType));
		}

		return condition;
	}

	// 진행중인 경조사 조건
	private BooleanExpression createOngoingCondition(LocalDate nowDate, LocalTime nowTime, QCeremony ceremony) {
		BooleanExpression startDateCondition = ceremony.startDate.loe(nowDate);

		// 시간이 null인 경우
		BooleanExpression noTimeCondition = ceremony.startTime.isNull()
			.and(
				ceremony.endDate.isNull().and(ceremony.startDate.eq(nowDate))
					.or(ceremony.endDate.isNotNull().and(ceremony.endDate.goe(nowDate))));

		// 시간이 not null인 경우
		BooleanExpression withTimeCondition = ceremony.startTime.isNotNull()
			.and(
				ceremony.startDate.lt(nowDate).and(ceremony.endDate.gt(nowDate))
					.or(ceremony.startDate.eq(nowDate).and(ceremony.endDate.eq(nowDate))
						.and(ceremony.startTime.loe(nowTime)).and(ceremony.endTime.goe(nowTime)))
					.or(ceremony.startDate.eq(nowDate).and(ceremony.endDate.gt(nowDate))
						.and(ceremony.startTime.loe(nowTime)))
					.or(ceremony.startDate.lt(nowDate).and(ceremony.endDate.eq(nowDate))
						.and(ceremony.endTime.goe(nowTime))));

		return startDateCondition.and(noTimeCondition.or(withTimeCondition));
	}

	// 곧 다가올 경조사 조건
	private BooleanExpression createUpcomingCondition(LocalDate nowDate, LocalTime nowTime, QCeremony ceremony) {
		return ceremony.startDate.gt(nowDate)
			.or(ceremony.startDate.eq(nowDate)
				.and(ceremony.startTime.isNotNull())
				.and(ceremony.startTime.gt(nowTime)));
	}

	// 지난 경조사 조건
	private BooleanExpression createPastCondition(LocalDate nowDate, LocalTime nowTime, QCeremony ceremony) {
		// 시간이 null인 경우
		BooleanExpression noTimeCondition = ceremony.startTime.isNull()
			.and(
				ceremony.endDate.isNull().and(ceremony.startDate.lt(nowDate))
					.or(ceremony.endDate.isNotNull().and(ceremony.endDate.lt(nowDate))));

		// 시간이 not null인 경우
		BooleanExpression withTimeCondition = ceremony.startTime.isNotNull()
			.and(
				ceremony.endDate.lt(nowDate)
					.or(ceremony.endDate.eq(nowDate).and(ceremony.endTime.lt(nowTime))));

		return noTimeCondition.or(withTimeCondition);
	}
}