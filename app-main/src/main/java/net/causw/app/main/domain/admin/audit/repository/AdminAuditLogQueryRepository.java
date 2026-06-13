package net.causw.app.main.domain.admin.audit.repository;

import static net.causw.app.main.domain.user.account.entity.user.QUserAdminActionLog.userAdminActionLog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.domain.user.account.entity.user.UserAdminActionLog;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;

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
		if (condition.category() != null && condition.category() != AdminAuditLogCategory.USER) {
			return Page.empty(pageable);
		}

		UserAdminActionType actionType = parseActionType(condition.actionType());
		if (condition.actionType() != null && actionType == null) {
			return Page.empty(pageable);
		}

		BooleanBuilder where = new BooleanBuilder()
			.and(createdAtGoe(condition))
			.and(createdAtLoe(condition))
			.and(actionTypeEq(actionType))
			.and(emailKeywordContains(condition.keyword()));

		List<UserAdminActionLog> logs = jpaQueryFactory
			.selectFrom(userAdminActionLog)
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(userAdminActionLog.createdAt.desc())
			.fetch();

		List<AdminAuditLogItem> content = logs.stream()
			.map(this::toItem)
			.toList();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(userAdminActionLog.count())
			.from(userAdminActionLog)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private UserAdminActionType parseActionType(String actionType) {
		if (actionType == null) {
			return null;
		}
		try {
			return UserAdminActionType.valueOf(actionType.toUpperCase());
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private BooleanExpression createdAtGoe(AdminAuditLogCondition condition) {
		return condition.from() == null ? null : userAdminActionLog.createdAt.goe(condition.from());
	}

	private BooleanExpression createdAtLoe(AdminAuditLogCondition condition) {
		return condition.to() == null ? null : userAdminActionLog.createdAt.loe(condition.to());
	}

	private BooleanExpression actionTypeEq(UserAdminActionType actionType) {
		return actionType == null ? null : userAdminActionLog.actionType.eq(actionType);
	}

	private BooleanExpression emailKeywordContains(String keyword) {
		if (keyword == null) {
			return null;
		}
		return userAdminActionLog.adminUserEmail.containsIgnoreCase(keyword)
			.or(userAdminActionLog.targetUserEmail.containsIgnoreCase(keyword));
	}

	private AdminAuditLogItem toItem(UserAdminActionLog log) {
		return new AdminAuditLogItem(
			log.getId(),
			AdminAuditLogCategory.USER,
			log.getAdminUserId(),
			log.getAdminUserEmail(),
			log.getTargetUserId(),
			log.getTargetUserEmail(),
			log.getActionType(),
			log.getBeforeState(),
			log.getAfterState(),
			log.getBeforeRoles(),
			log.getAfterRoles(),
			log.getReason(),
			log.getCreatedAt());
	}
}
