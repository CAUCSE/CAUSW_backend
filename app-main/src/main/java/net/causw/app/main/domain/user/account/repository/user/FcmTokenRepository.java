package net.causw.app.main.domain.user.account.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.user.account.entity.user.FcmToken;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {

	/**
	 * 토큰 value로 fcm 토큰 찾기
	 * @param tokenValue fcm 토큰 value
	 * @return fcm 토큰 엔티티
	 */
	Optional<FcmToken> findByTokenValue(String tokenValue);

	/**
	 * 특정 user의 전체 fcm 토큰 삭제
	 * @param user 대상 유저
	 */
	void deleteAllByUser(User user);
}
