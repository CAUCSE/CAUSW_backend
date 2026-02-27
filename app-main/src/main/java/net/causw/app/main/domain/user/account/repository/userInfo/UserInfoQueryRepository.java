package net.causw.app.main.domain.user.account.repository.userInfo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.QUuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.QUserProfileImage;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.userInfo.QUserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.QUserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserInfoQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<UserInfo> findAllWithFilter(UserInfoListCondition filter, Pageable pageable) {
		QUserInfo userInfo = QUserInfo.userInfo;
		QUser user = QUser.user;
		QUserProfileImage userProfileImage = QUserProfileImage.userProfileImage;
		QUuidFile uuidFile = QUuidFile.uuidFile;

		BooleanExpression condition = baseCondition(filter, userInfo);

		List<UserInfo> content = jpaQueryFactory
			.selectFrom(userInfo)
			.join(userInfo.user, user).fetchJoin()
			.leftJoin(user.userProfileImage, userProfileImage).fetchJoin()
			.leftJoin(userProfileImage.uuidFile, uuidFile).fetchJoin()
			.where(condition)
			.orderBy(getSortType(filter, userInfo))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(userInfo.count())
			.from(userInfo)
			.join(userInfo.user, user)
			.where(condition);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression baseCondition(UserInfoListCondition filter, QUserInfo userInfo) {
		BooleanExpression condition = Expressions.TRUE.isTrue();
		List<AcademicStatus> academicStatusList = filter.academicStatus();
		Integer admissionYearStart = filter.admissionYearStart();
		Integer admissionYearEnd = filter.admissionYearEnd();
		String keyword = filter.keyword();

		// 학적 상태 필터
		if (academicStatusList == null || academicStatusList.isEmpty()) {
			condition = condition.and(userInfo.user.academicStatus.in(AcademicStatus.GRADUATED, AcademicStatus.ENROLLED,
				AcademicStatus.LEAVE_OF_ABSENCE));
		} else {
			condition = condition.and(userInfo.user.academicStatus.in(academicStatusList));
		}

		// 학번 필터
		if (!(admissionYearStart == null && admissionYearEnd == null)) {
			if (admissionYearStart == null || admissionYearEnd == null) {
				throw UserInfoErrorCode.INVALID_ADMISSION_YEAR_RANGE.toBaseException();
			}
			if (admissionYearStart > admissionYearEnd) {
				throw UserInfoErrorCode.INVALID_ADMISSION_YEAR_RANGE.toBaseException();
			}
			condition = condition.and(userInfo.user.admissionYear.between(admissionYearStart, admissionYearEnd));
		}

		// 검색
		QUserCareer userCareer = QUserCareer.userCareer;
		if (keyword != null && !keyword.trim().isBlank()) {
			BooleanExpression keywordCondition = Expressions.FALSE.isTrue();

			keywordCondition = keywordCondition.or(userInfo.user.name.containsIgnoreCase(keyword));
			keywordCondition = keywordCondition.or(userInfo.job.containsIgnoreCase(keyword));
			keywordCondition = keywordCondition.or(JPAExpressions.selectFrom(userCareer)
				.where(userCareer.userInfo.eq(userInfo)
					.and(userCareer.description.containsIgnoreCase(keyword)))
				.exists());
			condition = condition.and(keywordCondition);
		}

		return condition;
	}

	// 정렬 필터
	private OrderSpecifier<?>[] getSortType(UserInfoListCondition filter, QUserInfo userInfo) {
		if (filter.sortType() == null) {
			return new OrderSpecifier[] {
				userInfo.updatedAt.desc(), userInfo.user.admissionYear.desc()
			};
		}

		switch (filter.sortType()) {
			case ADMISSION_YEAR_DESC -> {
				return new OrderSpecifier[] {
					userInfo.user.admissionYear.desc(), userInfo.updatedAt.desc()
				};
			}
			case ADMISSION_YEAR_ASC -> {
				return new OrderSpecifier[] {
					userInfo.user.admissionYear.asc(), userInfo.updatedAt.desc()
				};
			}
			case UPDATED_AT_ASC -> {
				return new OrderSpecifier[] {
					userInfo.updatedAt.asc(), userInfo.user.admissionYear.desc()
				};
			}
			default -> {
				return new OrderSpecifier[] {
					userInfo.updatedAt.desc(), userInfo.user.admissionYear.desc()
				};
			}
		}
	}
}
