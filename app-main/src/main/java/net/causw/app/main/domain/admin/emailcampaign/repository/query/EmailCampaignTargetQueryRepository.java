package net.causw.app.main.domain.admin.emailcampaign.repository.query;

import static net.causw.app.main.domain.user.account.entity.user.QUser.user;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignFilter;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailCampaignTargetQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 활성·미삭제·유효 이메일 사용자를 대상으로 필터 항목 간 AND 조건을 적용한다.
	 * @param filter 입학연도, 학과, 학적 상태 필터
	 * @return 사용자 ID와 이메일을 포함한 발송 대상 스냅샷 목록
	 */
	public List<EmailCampaignTarget> findTargets(EmailCampaignFilter filter) {
		BooleanBuilder where = new BooleanBuilder()
			.and(user.state.eq(UserState.ACTIVE))
			.and(user.deletedAt.isNull())
			.and(user.email.isNotNull())
			.and(user.email.ne(""))
			.and(admissionYearIn(filter))
			.and(departmentIn(filter))
			.and(academicStatusIn(filter));

		return jpaQueryFactory
			.select(Projections.constructor(
				EmailCampaignTarget.class,
				user.id,
				user.email,
				user.admissionYear,
				user.department,
				user.academicStatus))
			.from(user)
			.where(where)
			.orderBy(user.id.asc())
			.fetch();
	}

	private BooleanExpression admissionYearIn(EmailCampaignFilter filter) {
		return filter.admissionYears().isEmpty() ? null : user.admissionYear.in(filter.admissionYears());
	}

	private BooleanExpression departmentIn(EmailCampaignFilter filter) {
		return filter.departments().isEmpty() ? null : user.department.in(filter.departments());
	}

	private BooleanExpression academicStatusIn(EmailCampaignFilter filter) {
		return filter.academicStatuses().isEmpty() ? null : user.academicStatus.in(filter.academicStatuses());
	}
}
