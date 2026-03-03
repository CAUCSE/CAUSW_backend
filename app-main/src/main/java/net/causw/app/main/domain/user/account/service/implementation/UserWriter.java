package net.causw.app.main.domain.user.account.service.implementation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;

	public User save(User user) {
		return this.userRepository.save(user);
	}

	public SocialAccount save(SocialAccount socialAccount) {
		return socialAccountRepository.save(socialAccount);
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

	public User dropByAdmin(User user, String dropReason) {
		user.dropByAdmin(dropReason);
		return this.userRepository.save(user);
	}

	public User restore(User user) {
		user.restore();
		return this.userRepository.save(user);
	}

	// 관리자 전용 권한 교체 메서드
	public User replaceRole(User user, Role currentRole, Role newRole) {
		Set<Role> roles = new HashSet<>(user.getRoles());
		roles.remove(currentRole);
		roles.add(newRole);
		user.setRoles(roles);
		return this.userRepository.save(user);
	}
}
