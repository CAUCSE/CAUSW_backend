package net.causw.app.main.domain.user.account.service.implementation;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.SocialType;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.repository.user.query.UserQueryRepository;
import net.causw.app.main.domain.user.account.service.dto.request.DeletedUserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.dto.result.DeletedUserListItemDto;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserReader {
	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;

	/**
	 * 유저 ID로 유저 조회(삭제 여부 상관 없음)
	 *
	 * @param userId 유저 ID
	 * @return 유저 Entity
	 */
	public User findUserById(String userId) {
		return userRepository.findById(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	/**
	 * 유저 ID로 유저 조회(삭제된 유저는 제외)
	 *
	 * @param userId 유저 ID
	 * @return 유저 Entity
	 */
	public User findUserByIdNotDeleted(String userId) {
		return userQueryRepository.findByIdNotDeleted(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	public User findAdminUserById(String userId) {
		// todo: roles Conataining 검색에 성능 이슈 있는지 체크 필요
		return userRepository.findByIdAndRolesContaining(userId, Role.ADMIN)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	public List<User> findUsersByIds(List<String> userIds) {
		return userQueryRepository.findByIds(userIds);
	}

	public Optional<User> checkUserExistByPhoneNumAndName(String phoneNum, String name) {
		return userRepository.findByPhoneNumberAndName(phoneNum, name);
	}

	public User findByEmailOrElseThrow(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(UserErrorCode.INVALID_LOGIN::toBaseException);

	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findByEmailAndName(String email, String name) {
		return userRepository.findByEmailAndName(email, name)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	public List<User> searchByCondition(UserQueryCondition condition) {
		return userQueryRepository.searchByCondition(condition);
	}

	/**
	 * 유저 목록을 조회합니다. 이메일/이름/학번 키워드 검색, 상태 필터링, 학적 상태 필터링을 지원합니다.
	 * @param condition 조회 조건 (예: 키워드, 상태, 학적 상태 등)
	 * @param pageable 페이지네이션 정보
	 * @return 유저 목록 페이지
	 */
	public Page<UserListItem> findUserList(UserListCondition condition, Pageable pageable) {
		return userQueryRepository.findUserList(condition, pageable)
			.map(UserListItem::from);
	}

	/**
	 * 삭제된 유저 목록을 조회합니다.
	 * @param condition 조회 조건 (예: 삭제된 날짜 범위, 키워드 등)
	 * @param pageable 페이지네이션 정보
	 * @return 삭제된 유저 목록 페이지
	 */
	public Page<DeletedUserListItemDto> findDeletedUserList(
		DeletedUserQueryCondition condition,
		Pageable pageable) {
		return userQueryRepository.findDeletedUserList(condition, pageable)
			.map(DeletedUserListItemDto::from);
	}

	/**
	 * 신고된 유저 목록을 조회합니다. 키워드 검색, 상태 필터링, 학적 상태 필터링을 지원합니다.
	 * @param keyword 이메일/이름/학번 키워드 검색 (null 또는 빈 문자열인 경우 검색 제외)
	 * @param state 사용자 상태 필터 (null인 경우 모든 상태 포함)
	 * @param academicStatus 학적 상태 필터 (null인 경우 모든 학적 상태 포함)
	 * @param pageable 페이지네이션 정보
	 * @return 신고된 유저 목록 페이지
	 */
	public Page<User> findReportedUserList(
		String keyword,
		UserState state,
		AcademicStatus academicStatus,
		Pageable pageable) {
		return userQueryRepository.findReportedUserList(
			normalizeKeyword(keyword),
			state,
			academicStatus,
			pageable);
	}

	// 상세 조회용 (fetch join)
	public User findDetailById(String userId) {
		return userQueryRepository.findByIdWithRelations(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	public Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId) {
		return socialAccountRepository.findBySocialIdAndSocialType(socialId, socialType)
			.map(SocialAccount::getUser);
	}

	/**
	 * 입학년도 목록에 해당하는 유저를 조회합니다.
	 * @param admissionYears 조회할 입학년도 목록
	 * @return 해당 입학년도 유저 목록
	 */
	public List<User> findUsersByAdmissionYears(Collection<Integer> admissionYears) {
		return userQueryRepository.findByAdmissionYearIn(admissionYears);
	}

	/**
	 * 모든 활동 가능한 유저를 조회합니다.
	 * @return 활성 유저 목록
	 */
	public List<User> findAllActive() {
		return userQueryRepository.findAllActive();
	}

	/**
	 * 특정 학적 상태에 해당하는 관리자 유저 목록을 조회합니다.
	 * @param academicStatus 조회할 학적 상태
	 * @return 해당 학적 상태에 해당하는 관리자 유저 목록
	 */
	public List<User> findAdminsByAcademicStatus(AcademicStatus academicStatus) {
		return userQueryRepository.findAdminsByAcademicStatus(academicStatus);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean existsByEmailAndName(String email, String name) {
		return userRepository.findByEmailAndName(email, name).isPresent();
	}

	public Optional<User> findByEmailAndNameOptional(String email, String name) {
		return userRepository.findByEmailAndName(email, name);
	}

	public Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
		return userRepository.countByCreatedAtBetween(start, end);
	}

	public Long getTotalUserCount() {
		return userQueryRepository.countTotalUsers();
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return keyword.trim();
	}
}
