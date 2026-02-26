package net.causw.app.main.domain.user.account.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;

	public User save(User user) {
		return this.userRepository.save(user);
	}

	/**
	 * 사용자 상태를 AWAIT으로 변경하고 저장합니다.
	 * (REJECT에서 재학인증 재신청 시 사용)
	 */
	public User updateStateToAwait(User user) {
		user.markAsAwait();
		return this.userRepository.save(user);
	}

	/**
	 * v2 재학인증 승인 시 유저 정보를 갱신합니다.
	 * - UserAdmission.requested 필드 기반으로 User 정보 업데이트
	 * - User.userState = ACTIVE
	 * - User.role = COMMON
	 */
	public User approveAdmission(User user, UserAdmission admission) {
		user.approveAdmission(admission);
		return this.userRepository.save(user);
	}

	/**
	 * v2 재학인증 거절 시 유저 상태를 변경합니다.
	 * - User.userState = REJECT
	 * - User.rejectOrDropReason 기록
	 */
	public User rejectAdmission(User user, String rejectReason) {
		user.rejectAdmission(rejectReason);
		return this.userRepository.save(user);
	}
}
