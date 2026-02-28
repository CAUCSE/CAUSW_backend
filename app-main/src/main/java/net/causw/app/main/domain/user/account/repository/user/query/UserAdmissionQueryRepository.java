package net.causw.app.main.domain.user.account.repository.user.query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.QUserAdmission;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserAdmissionQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 관리자 재학인증 신청 목록 조회 (페이징 + 필터링)
	 */
	public Page<UserAdmission> findAdmissionList(
		String keyword,
		UserState userState,
		Pageable pageable) {

		QUserAdmission admission = QUserAdmission.userAdmission;
		QUser user = QUser.user;

		BooleanBuilder where = new BooleanBuilder();

		if (keyword != null && !keyword.isBlank()) {
			where.and(
				user.name.containsIgnoreCase(keyword)
					.or(admission.requestedStudentId.containsIgnoreCase(keyword)));
		}

		if (userState != null) {
			where.and(user.state.eq(userState));
		}

		List<UserAdmission> content = jpaQueryFactory
			.selectFrom(admission)
			.leftJoin(admission.user, user).fetchJoin()
			.where(where)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(admission.createdAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(admission.count())
			.from(admission)
			.leftJoin(admission.user, user)
			.where(where);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	/**
	 * 관리자 재학인증 신청 상세 조회 (첨부 이미지 포함)
	 */
	public Optional<UserAdmission> findByIdWithDetails(String admissionId) {
		QUserAdmission admission = QUserAdmission.userAdmission;
		QUser user = QUser.user;

		UserAdmission result = jpaQueryFactory
			.selectFrom(admission)
			.leftJoin(admission.user, user).fetchJoin()
			.leftJoin(admission.userAdmissionAttachImageList).fetchJoin()
			.where(admission.id.eq(admissionId))
			.fetchOne();

		return Optional.ofNullable(result);
	}
}
