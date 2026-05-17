package net.causw.app.main.domain.user.account.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialAccountWriter {

	private final SocialAccountRepository socialAccountRepository;

	/**
	 * 제공된 사용자 목록에 해당하는 모든 소셜 계정 정보를 삭제합니다.
	 * <p>
	 * 계정 영구 삭제(Hard Delete) 시점에 더 이상 필요하지 않은
	 * 소셜 연동 정보를 데이터베이스에서 제거하기 위해 사용합니다.
	 * </p>
	 *
	 * @param users 소셜 계정을 삭제할 사용자 엔티티 목록
	 */
	@Transactional
	public void deleteSocialAccountsByUsers(List<User> users) {
		List<String> userIds = users.stream()
			.map(User::getId)
			.toList();

		if (userIds.isEmpty()) {
			return;
		}

		List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUserIdIn(userIds);

		if (!socialAccounts.isEmpty()) {
			socialAccountRepository.deleteAll(socialAccounts);
		}
	}

	public void save(SocialAccount socialAccount) {
		socialAccountRepository.save(socialAccount);
	}

	/**
	 * 단일 소셜 계정 연동 정보를 삭제합니다.
	 * 마이페이지에서 사용자가 직접 특정 provider 연동을 해제할 때 사용합니다.
	 *
	 * @param socialAccount 삭제할 소셜 계정 엔티티
	 */
	public void deleteSocialAccount(SocialAccount socialAccount) {
		socialAccountRepository.delete(socialAccount);
	}
}
