package net.causw.app.main.domain.user.account.repository.userInfo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.entity.userInfo.QUserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserInfoQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<UserInfo> findAllWithFilter(UserInfoListCondition filter, Pageable pageable) {
		QUserInfo userInfo = QUserInfo.userInfo;
		BooleanExpression condition = baseCondition(filter, userInfo);

		List<UserInfo> content = jpaQueryFactory
			.selectFrom(userInfo)
			.where(condition)
			.orderBy(getSortType(filter, userInfo))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(userInfo.count())
			.from(userInfo)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression baseCondition(UserInfoListCondition filter, QUserInfo userInfo) {
		BooleanExpression condition;

		// 학적 상태 선택 안 되어 있으면 모두 조회 (재학생, 휴학생, 졸업생)
		if (filter.academicStatus() == null || filter.academicStatus().isEmpty()) {
			condition = userInfo.user.academicStatus.in(AcademicStatus.GRADUATED, AcademicStatus.ENROLLED,
				AcademicStatus.LEAVE_OF_ABSENCE);
		} else {
			condition = userInfo.user.academicStatus.in(filter.academicStatus());
		}

		// 학번 범위 설정 안 되어 있으면 모두 조회
		if (filter.admissionYearStart() == null && filter.admissionYearEnd() == null) {
			return condition;
		}
		if (filter.admissionYearStart() == null || filter.admissionYearEnd() == null) {
			throw UserInfoErrorCode.INVALID_ADMISSION_YEAR_RANGE.toBaseException();
		}
		if (filter.admissionYearStart() > filter.admissionYearEnd()) {
			throw UserInfoErrorCode.INVALID_ADMISSION_YEAR_RANGE.toBaseException();
		}

		return condition
			.and(userInfo.user.admissionYear.between(filter.admissionYearStart(), filter.admissionYearEnd()));
	}

	private OrderSpecifier<?> getSortType(UserInfoListCondition filter, QUserInfo userInfo) {
		// TODO: 기준 필요
		if (filter.sortType() == null) {
			return userInfo.user.name.asc();
		}

		return filter.sortType() == SortType.DESC ? userInfo.user.name.desc() : userInfo.user.name.asc();
	}
}
