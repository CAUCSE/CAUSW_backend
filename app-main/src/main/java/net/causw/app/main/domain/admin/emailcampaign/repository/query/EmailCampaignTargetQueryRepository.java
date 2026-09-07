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
