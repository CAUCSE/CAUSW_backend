package net.causw.app.main.domain.user.academic.repository.userAcademicRecord.query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.QUuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.QUserAcademicRecordApplicationAttachImage;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.QUserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.enums.user.Department;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserAcademicRecordApplicationQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 졸업 -> 재학 복학 신청 목록 조회 (필터링 및 검색 지원)
	 * - 현재 학적상태가 졸업(GRADUATED)인 사용자가 재학(ENROLLED)으로 변경 신청한 건만 조회
	 */
	public Page<UserAcademicRecordApplication> searchApplications(
		AcademicRecordRequestStatus requestStatus,
		Department department,
		String keyword,
		Pageable pageable) {
		QUserAcademicRecordApplication application = QUserAcademicRecordApplication.userAcademicRecordApplication;
		QUser user = QUser.user;

		BooleanBuilder predicate = new BooleanBuilder();

		// 필수 조건: 졸업 -> 재학 신청
		predicate.and(application.targetAcademicStatus.eq(AcademicStatus.ENROLLED));
		predicate.and(user.academicStatus.eq(AcademicStatus.GRADUATED));

		// 선택 조건: 신청 상태 필터
		if (requestStatus != null) {
			predicate.and(application.academicRecordRequestStatus.eq(requestStatus));
		}

		// 선택 조건: 학과 필터
		if (department != null) {
			predicate.and(user.department.eq(department));
		}

		// 선택 조건: 키워드 검색 (이름 또는 학번)
		if (keyword != null && !keyword.isBlank()) {
			predicate.and(
				user.name.containsIgnoreCase(keyword)
					.or(user.studentId.containsIgnoreCase(keyword)));
		}

		// 데이터 조회
		List<UserAcademicRecordApplication> content = jpaQueryFactory
			.selectFrom(application)
			.join(application.user, user).fetchJoin()
			.where(predicate)
			.orderBy(application.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 전체 개수 조회
		Long total = jpaQueryFactory
			.select(application.count())
			.from(application)
			.join(application.user, user)
			.where(predicate)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	/**
	 * 학적 변경 신청 상세 조회 (연관 엔티티 fetch join)
	 * - user, attachImageList, uuidFile을 한 번의 쿼리로 조회하여 N+1 문제 방지
	 */
	public Optional<UserAcademicRecordApplication> findByIdWithDetails(String applicationId) {
		QUserAcademicRecordApplication application = QUserAcademicRecordApplication.userAcademicRecordApplication;
		QUserAcademicRecordApplicationAttachImage attachImage = QUserAcademicRecordApplicationAttachImage.userAcademicRecordApplicationAttachImage;
		QUuidFile uuidFile = QUuidFile.uuidFile;

		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(application)
			.leftJoin(application.user).fetchJoin()
			.leftJoin(application.userAcademicRecordAttachImageList, attachImage).fetchJoin()
			.leftJoin(attachImage.uuidFile, uuidFile).fetchJoin()
			.where(application.id.eq(applicationId))
			.fetchOne());
	}
}
