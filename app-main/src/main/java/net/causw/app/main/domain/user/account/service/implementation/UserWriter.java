package net.causw.app.main.domain.user.account.service.implementation;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserWriter {

	private final UserRepository userRepository;
	private final SocialAccountReader socialAccountReader;
	private final SocialAccountUnlinkManager socialAccountUnlinkManager;

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

	public User withdraw(User user) {
		user.withdraw(LocalDateTime.now());
		return this.userRepository.save(user);
	}

	public User dropByAdmin(User user, String dropReason) {
		user.dropByAdmin(dropReason, LocalDateTime.now());
		return this.userRepository.save(user);
	}

	public User restore(User user) {
		user.restore();
		return this.userRepository.save(user);
	}

	/**
	 * 이메일 인증 완료 처리
	 * - isEmailVerified = true 로 변경
	 */
	public User markEmailAsVerified(User user) {
		user.markEmailAsVerified();
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

	/**
	 * 탈퇴한 사용자들의 개인정보를 익명화하고 소셜 연동을 최종 해제합니다.
	 * <p>
	 * 실시간 탈퇴 시 실패했을 수 있는 소셜 해제를 재시도하며,
	 * 유예 기간이 지난 사용자의 식별 정보를 마스킹 처리하여 법적 개인정보 파기 의무를 수행합니다.
	 * </p>
	 *
	 * @param users 익명화 처리를 진행할 사용자 엔티티 목록
	 */
	@Transactional
	public void cleanupWithdrawnUsers(List<User> users) {
		List<String> userIds = users.stream().map(User::getId).toList();

		Map<String, List<SocialAccount>> socialAccountMap = socialAccountReader.findAllByUserIdIn(userIds)
			.stream()
			.collect(Collectors.groupingBy(sa -> sa.getUser().getId()));

		for (User user : users) {
			if (isAlreadyAnonymized(user)) {
				continue;
			}

			List<SocialAccount> socialAccounts = socialAccountMap.getOrDefault(user.getId(), List.of());

			for (SocialAccount socialAccount : socialAccounts) {
				try {
					socialAccountUnlinkManager.unlink(socialAccount);
				} catch (Exception e) {
					log.error("[유저 정리 배치] 소셜 연동 해제 실패 - userId: {}, socialType: {}",
						user.getId(), socialAccount.getSocialType(), e);
				}
			}

			user.anonymize();
		}

		userRepository.saveAll(users);
	}

	private boolean isAlreadyAnonymized(User user) {
		return user.isDeleted() &&
			user.getEmail() != null &&
			user.getEmail().startsWith("deleted_");
	}
}
