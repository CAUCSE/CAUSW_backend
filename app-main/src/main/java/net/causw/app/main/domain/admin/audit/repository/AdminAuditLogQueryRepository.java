package net.causw.app.main.domain.admin.audit.repository;

import static net.causw.app.main.domain.admin.audit.entity.QAdminAuditLog.adminAuditLog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.audit.entity.AdminAuditLog;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminAuditLogQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<AdminAuditLogItem> findAuditLogs(AdminAuditLogCondition condition, Pageable pageable) {
		BooleanBuilder where = new BooleanBuilder()
			.and(createdAtGoe(condition))
			.and(createdAtLoe(condition))
			.and(categoryEq(condition))
			.and(actionTypeEq(condition))
			.and(keywordContains(condition.keyword()));

		List<AdminAuditLog> logs = jpaQueryFactory
			.selectFrom(adminAuditLog)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(adminAuditLog.createdAt.desc())
			.fetch();

		List<AdminAuditLogItem> content = logs.stream()
			.map(this::toItem)
			.toList();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(adminAuditLog.count())
			.from(adminAuditLog)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression createdAtGoe(AdminAuditLogCondition condition) {
		return condition.from() == null ? null : adminAuditLog.createdAt.goe(condition.from());
	}

	private BooleanExpression createdAtLoe(AdminAuditLogCondition condition) {
		return condition.to() == null ? null : adminAuditLog.createdAt.loe(condition.to());
	}

	private BooleanExpression categoryEq(AdminAuditLogCondition condition) {
		return condition.category() == null ? null : adminAuditLog.category.eq(condition.category());
	}

	private BooleanExpression actionTypeEq(AdminAuditLogCondition condition) {
		return condition.actionType() == null ? null : adminAuditLog.actionType.eq(condition.actionType());
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null) {
			return null;
		}
		return adminAuditLog.actorEmail.containsIgnoreCase(keyword)
			.or(adminAuditLog.actorName.containsIgnoreCase(keyword))
			.or(adminAuditLog.actorStudentId.containsIgnoreCase(keyword))
			.or(adminAuditLog.targetEmail.containsIgnoreCase(keyword))
			.or(adminAuditLog.targetName.containsIgnoreCase(keyword))
			.or(adminAuditLog.targetStudentId.containsIgnoreCase(keyword));
	}

	private AdminAuditLogItem toItem(AdminAuditLog log) {
		return new AdminAuditLogItem(
			log.getId(),
			log.getCategory(),
			log.getActionType(),
			log.getActionDescription(),
			log.getActorUserId(),
			log.getActorEmail(),
			log.getActorName(),
			log.getActorStudentId(),
			log.getTargetType(),
			log.getTargetId(),
			log.getTargetEmail(),
			log.getTargetName(),
			log.getTargetStudentId(),
			log.getSummary(),
			log.getMetadataJson(),
			log.getCreatedAt());
	}
}
