package net.causw.app.main.repository.userInfo.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.user.QUser;
import net.causw.app.main.domain.model.entity.userInfo.QUserCareer;
import net.causw.app.main.domain.model.entity.userInfo.QUserInfo;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.domain.model.entity.uuidFile.QUuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.QUserProfileImage;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.dto.userInfo.UserInfoSearchConditionDto;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserInfoQueryRepositoryImpl implements UserInfoQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<UserInfo> searchUserInfo(UserInfoSearchConditionDto userInfoSearchCondition, Pageable pageable) {
		// keyword는 이름, 직업, 경력, like 검색
		// user, job, user.carrer
		QUserInfo userInfo = QUserInfo.userInfo;
		BooleanBuilder predicate = buildSearchPredicate(userInfoSearchCondition, userInfo);

		QUser user = QUser.user;
		QUserProfileImage userProfileImage = QUserProfileImage.userProfileImage;
		QUuidFile uuidFile = QUuidFile.uuidFile;

		List<UserInfo> content = jpaQueryFactory
			.selectFrom(userInfo)
			.join(userInfo.user, user).fetchJoin()
			.leftJoin(user.userProfileImage, userProfileImage).fetchJoin()
			.leftJoin(userProfileImage.uuidFile, uuidFile).fetchJoin()
			.leftJoin(user.ceremonyNotificationSetting).fetchJoin()
			.leftJoin(user.locker).fetchJoin()
			.where(user.state.eq(UserState.ACTIVE))
			.where(predicate)
			.orderBy(userInfo.updatedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.distinct()
			.fetch();

		Long total = jpaQueryFactory
			.select(userInfo.count())
			.from(userInfo)
			.where(predicate)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0);
	}

	/**
	 * 검색 조건 생성
	 * (학번, 학적상태 (in), 이름 or 직업 or 커리어,텍스트 검색)
	 * @param userInfoSearchCondition 검색 조건 dto
	 * @param userInfo 검색 QueryDSL 엔티티
	 * @return 검색 조건 booleanBuilder
	 */
	private BooleanBuilder buildSearchPredicate(
		UserInfoSearchConditionDto userInfoSearchCondition,
		QUserInfo userInfo
	) {
		BooleanBuilder predicate = new BooleanBuilder();
		String keyword = userInfoSearchCondition.keyword();

		QUserCareer userCareer = QUserCareer.userCareer;
		if (keyword != null && !keyword.trim().isEmpty()) {
			BooleanBuilder keywordPredicate = new BooleanBuilder();
			keywordPredicate.or(userInfo.user.name.containsIgnoreCase(keyword));
			keywordPredicate.or(userInfo.job.containsIgnoreCase(keyword));
			keywordPredicate.or(JPAExpressions.selectFrom(userCareer)
				.where(userCareer.userInfo.eq(userInfo)
					.and(userCareer.description.containsIgnoreCase(keyword)))
				.exists());

			predicate.and(keywordPredicate);
		}

		// 입학 년도 검색
		Integer admissionYearStart = userInfoSearchCondition.admissionYearStart();
		Integer admissionYearEnd = userInfoSearchCondition.admissionYearEnd();
		if (
			admissionYearStart != null && admissionYearEnd != null
		) {
			BooleanBuilder admissionYearPredicate = new BooleanBuilder();
			admissionYearPredicate.and(userInfo.user.admissionYear.between(admissionYearStart, admissionYearEnd));

			predicate.and(admissionYearPredicate);
		}

		// 학적 상태
		List<AcademicStatus> academicStatuses = userInfoSearchCondition.academicStatus();
		if (academicStatuses != null && !academicStatuses.isEmpty()) {
			BooleanBuilder academicStatusPredicate = new BooleanBuilder();
			academicStatusPredicate.and(userInfo.user.academicStatus.in(academicStatuses));

			predicate.and(academicStatusPredicate);
		}
		return predicate;
	}
}
