package net.causw.app.main.domain.admin.emailcampaign.repository.query;

import static net.causw.app.main.domain.admin.emailcampaign.entity.QEmailCampaign.emailCampaign;
import static net.causw.app.main.domain.admin.emailcampaign.entity.QEmailCampaignRecipient.emailCampaignRecipient;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailCampaignQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<EmailCampaignSummary> findCampaigns(
		EmailCampaignStatus status,
		LocalDateTime from,
		LocalDateTime to,
		Pageable pageable) {
		BooleanBuilder where = new BooleanBuilder()
			.and(campaignStatusEq(status))
			.and(campaignCreatedAtGoe(from))
			.and(campaignCreatedAtLoe(to));

		List<EmailCampaignSummary> content = jpaQueryFactory
			.selectFrom(emailCampaign)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(emailCampaign.createdAt.desc())
			.fetch()
			.stream()
			.map(campaign -> new EmailCampaignSummary(
				campaign.getId(),
				campaign.getSubject(),
				campaign.getStatus(),
				campaign.getRecipientCount(),
				campaign.getPendingCount(),
				campaign.getSentCount(),
				campaign.getFailedCount(),
				campaign.getSkippedCount(),
				campaign.getCreatedAt(),
				campaign.getCompletedAt()))
			.toList();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(emailCampaign.count())
			.from(emailCampaign)
			.where(where);
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	public Page<EmailCampaignRecipientItem> findRecipients(
		String campaignId,
		EmailCampaignRecipientStatus status,
		Pageable pageable) {
		BooleanBuilder where = new BooleanBuilder()
			.and(emailCampaignRecipient.campaign.id.eq(campaignId))
			.and(recipientStatusEq(status));

		List<EmailCampaignRecipientItem> content = jpaQueryFactory
			.selectFrom(emailCampaignRecipient)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(emailCampaignRecipient.id.asc())
			.fetch()
			.stream()
			.map(recipient -> new EmailCampaignRecipientItem(
				recipient.getId(),
				recipient.getEmailSnapshot(),
				recipient.getStatus(),
				recipient.getAttemptCount(),
				recipient.getLastAttemptAt(),
				recipient.getSentAt(),
				recipient.getLastErrorCode()))
			.toList();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(emailCampaignRecipient.count())
			.from(emailCampaignRecipient)
			.where(where);
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression campaignStatusEq(EmailCampaignStatus status) {
		return status == null ? null : emailCampaign.status.eq(status);
	}

	private BooleanExpression campaignCreatedAtGoe(LocalDateTime from) {
		return from == null ? null : emailCampaign.createdAt.goe(from);
	}

	private BooleanExpression campaignCreatedAtLoe(LocalDateTime to) {
		return to == null ? null : emailCampaign.createdAt.loe(to);
	}

	private BooleanExpression recipientStatusEq(EmailCampaignRecipientStatus status) {
		return status == null ? null : emailCampaignRecipient.status.eq(status);
	}
}
