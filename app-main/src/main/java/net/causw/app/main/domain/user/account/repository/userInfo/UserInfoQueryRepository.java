package net.causw.app.main.domain.user.account.repository.userInfo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.userInfo.QUserCareer;
import net.causw.app.main.domain.user.account.entity.userInfo.QUserInfo;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;
import net.causw.app.main.domain.user.account.enums.userinfo.UserInfoSectionType;
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

	/**
	 * 검색 조건과 커서 위치를 기준으로 특정 동문 섹션을 조회한다.
	 * @param listCondition 검색 및 필터 조건
	 * @param positionId 마지막으로 조회한 동문 프로필 ID
	 * @param section 조회할 동문 섹션
	 * @param cursorSortType 커서에 저장된 정렬 기준
	 * @param updatedAt 마지막으로 조회한 프로필의 수정 일시
	 * @param admissionYear 마지막으로 조회한 사용자의 입학 연도
	 * @param name 마지막으로 조회한 사용자의 이름
	 * @param excludeUserId 조회 결과에서 제외할 사용자 ID
	 * @param size 조회할 최대 항목 수
	 * @return 다음 페이지 존재 여부를 포함한 동문 프로필 slice
	 */
	public Slice<UserInfo> findAllWithFilter(
		UserInfoListCondition listCondition,
		String positionId,
		UserInfoSectionType section,
		SortType cursorSortType,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String name,
		String excludeUserId,
		int size) {
		QUserInfo userInfo = QUserInfo.userInfo;
		QUser user = QUser.user;

		SortType sortType = resolveSortType(listCondition);
		if (cursorSortType != null && cursorSortType != sortType) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}

		BooleanExpression condition = baseCondition(listCondition, userInfo, excludeUserId);
		BooleanExpression sectionCondition = userInfo.isCoffeeChatAvailable.eq(
			section == UserInfoSectionType.COFFEE_CHAT_AVAILABLE);
		BooleanExpression cursorCondition = cursorCondition(
			userInfo,
			sortType,
			updatedAt,
			admissionYear,
			name,
			positionId);

		List<UserInfo> content = jpaQueryFactory
			.selectFrom(userInfo)
			.join(userInfo.user, user).fetchJoin()
			.where(condition, sectionCondition, cursorCondition)
			.orderBy(getCursorSortType(sortType, userInfo))
			.limit(size + 1L)
			.fetch();

		boolean hasNext = content.size() > size;
		if (hasNext) {
			content.remove(size);
		}

		return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
	}

	private BooleanExpression cursorCondition(
		QUserInfo userInfo,
		SortType sortType,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String name,
		String positionId) {
		if (updatedAt == null && admissionYear == null && name == null && positionId == null) {
			return null;
		}
		if (updatedAt == null || admissionYear == null || name == null || positionId == null) {
			throw UserInfoErrorCode.INVALID_CURSOR.toBaseException();
		}

		return switch (sortType) {
			case UPDATED_AT_DESC -> updatedAtDescCursorCondition(
				userInfo, updatedAt, admissionYear, positionId);
			case UPDATED_AT_ASC -> updatedAtAscCursorCondition(
				userInfo, updatedAt, admissionYear, positionId);
			case ADMISSION_YEAR_DESC -> admissionYearDescCursorCondition(
				userInfo, updatedAt, admissionYear, name, positionId);
			case ADMISSION_YEAR_ASC -> admissionYearAscCursorCondition(
				userInfo, updatedAt, admissionYear, name, positionId);
		};
	}

	private BooleanExpression updatedAtDescCursorCondition(
		QUserInfo userInfo,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String positionId) {
		return userInfo.updatedAt.lt(updatedAt)
			.or(userInfo.updatedAt.eq(updatedAt)
				.and(userInfo.user.admissionYear.lt(admissionYear)))
			.or(userInfo.updatedAt.eq(updatedAt)
				.and(userInfo.user.admissionYear.eq(admissionYear))
				.and(userInfo.id.lt(positionId)));
	}

	private BooleanExpression updatedAtAscCursorCondition(
		QUserInfo userInfo,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String positionId) {
		return userInfo.updatedAt.gt(updatedAt)
			.or(userInfo.updatedAt.eq(updatedAt)
				.and(userInfo.user.admissionYear.lt(admissionYear)))
			.or(userInfo.updatedAt.eq(updatedAt)
				.and(userInfo.user.admissionYear.eq(admissionYear))
				.and(userInfo.id.gt(positionId)));
	}

	private BooleanExpression admissionYearDescCursorCondition(
		QUserInfo userInfo,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String name,
		String positionId) {
		return userInfo.user.admissionYear.lt(admissionYear)
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.gt(name)))
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.eq(name))
				.and(userInfo.updatedAt.lt(updatedAt)))
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.eq(name))
				.and(userInfo.updatedAt.eq(updatedAt))
				.and(userInfo.id.lt(positionId)));
	}

	private BooleanExpression admissionYearAscCursorCondition(
		QUserInfo userInfo,
		LocalDateTime updatedAt,
		Integer admissionYear,
		String name,
		String positionId) {
		return userInfo.user.admissionYear.gt(admissionYear)
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.gt(name)))
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.eq(name))
				.and(userInfo.updatedAt.lt(updatedAt)))
			.or(userInfo.user.admissionYear.eq(admissionYear)
				.and(userInfo.user.name.eq(name))
				.and(userInfo.updatedAt.eq(updatedAt))
				.and(userInfo.id.gt(positionId)));
	}

	private OrderSpecifier<?>[] getCursorSortType(SortType sortType, QUserInfo userInfo) {
		return switch (sortType) {
			case UPDATED_AT_DESC -> new OrderSpecifier[] {
				userInfo.updatedAt.desc(),
				userInfo.user.admissionYear.desc(),
				userInfo.id.desc()
			};
			case UPDATED_AT_ASC -> new OrderSpecifier[] {
				userInfo.updatedAt.asc(),
				userInfo.user.admissionYear.desc(),
				userInfo.id.asc()
			};
			case ADMISSION_YEAR_DESC -> new OrderSpecifier[] {
				userInfo.user.admissionYear.desc(),
				userInfo.user.name.asc(),
				userInfo.updatedAt.desc(),
				userInfo.id.desc()
			};
			case ADMISSION_YEAR_ASC -> new OrderSpecifier[] {
				userInfo.user.admissionYear.asc(),
				userInfo.user.name.asc(),
				userInfo.updatedAt.desc(),
				userInfo.id.asc()
			};
		};
	}

	private SortType resolveSortType(UserInfoListCondition filter) {
		if (filter.sortType() == null || filter.sortType().isBlank()) {
			return SortType.UPDATED_AT_DESC;
		}
		return SortType.fromString(filter.sortType());
	}

	public Page<UserInfo> findAllWithFilter(UserInfoListCondition filter, Pageable pageable, String excludeUserId) {
		QUserInfo userInfo = QUserInfo.userInfo;
		QUser user = QUser.user;

		BooleanExpression condition = baseCondition(filter, userInfo, excludeUserId);

		List<UserInfo> content = jpaQueryFactory
			.selectFrom(userInfo)
			.join(userInfo.user, user).fetchJoin()
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

	private BooleanExpression baseCondition(UserInfoListCondition filter, QUserInfo userInfo, String excludeUserId) {
		BooleanExpression condition = Expressions.TRUE.isTrue();

		// 본인 프로필 제외
		if (excludeUserId != null) {
			condition = condition.and(userInfo.user.id.ne(excludeUserId));
		}
		List<String> academicStatusList = filter.academicStatus();
		Integer admissionYearStart = filter.admissionYearStart();
		Integer admissionYearEnd = filter.admissionYearEnd();
		String keyword = filter.keyword();

		// 학적 상태 필터
		if (academicStatusList == null || academicStatusList.isEmpty()) {
			condition = condition
				.and(userInfo.user.academicStatus.in(AcademicStatus.GRADUATED, AcademicStatus.ENROLLED));
		} else {
			List<AcademicStatus> academicStatuses = academicStatusList.stream()
				.map(AcademicStatus::fromString)
				.toList();
			condition = condition.and(userInfo.user.academicStatus.in(academicStatuses));
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
			keywordCondition = keywordCondition.or(userInfo.description.containsIgnoreCase(keyword));
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
		if (filter.sortType() == null || filter.sortType().isEmpty()) {
			return new OrderSpecifier[] {
				userInfo.updatedAt.desc(), userInfo.user.admissionYear.desc()
			};
		}
		SortType sortType = SortType.fromString(filter.sortType());

		switch (sortType) {
			case ADMISSION_YEAR_DESC -> {
				return new OrderSpecifier[] {
					userInfo.user.admissionYear.desc(), userInfo.user.name.asc(), userInfo.updatedAt.desc()
				};
			}
			case ADMISSION_YEAR_ASC -> {
				return new OrderSpecifier[] {
					userInfo.user.admissionYear.asc(), userInfo.user.name.asc(), userInfo.updatedAt.desc()
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
