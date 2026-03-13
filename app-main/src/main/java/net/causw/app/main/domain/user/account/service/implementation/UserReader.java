package net.causw.app.main.domain.user.account.service.implementation;

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
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
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
	 * @param userId 유저 ID
	 * @return 유저 Entity
	 */
	public User findUserById(String userId) {
		return userRepository.findById(userId)
			.orElseThrow(UserErrorCode.USER_NOT_FOUND::toBaseException);
	}

	/**
	 * 유저 ID로 유저 조회(삭제된 유저는 제외)
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

	public List<User> getUsersByIds(List<String> userIds) {
		return userQueryRepository.findByIds(userIds);
	}

	public List<User> searchByCondition(UserQueryCondition condition) {
		return userQueryRepository.searchByCondition(condition);
	}

	public Page<User> findUserList(
		UserListCondition condition,
		Pageable pageable) {
		return userQueryRepository.findUserList(
			normalizeKeyword(condition.keyword()),
			condition.state(),
			condition.academicStatus(),
			condition.department(),
			pageable);
	}

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

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean existsByEmailAndName(String email, String name) {
		return userRepository.findByEmailAndName(email, name).isPresent();
	}

	public Optional<User> findByEmailAndNameOptional(String email, String name) {
		return userRepository.findByEmailAndName(email, name);
  }
  
	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		return keyword.trim();
	}
}
