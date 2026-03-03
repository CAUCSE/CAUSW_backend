package net.causw.app.main.domain.user.relation.service.v2.implementation;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.relation.userBlock.UserBlockRepository;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockReader {

	private final UserBlockRepository userBlockRepository;

	/**
	 * 차단자 - 피차단자 Id 쌍으로 활성화된 차단이 있는지 확인하는 메서드
	 * @param blocker 차단자
	 * @param blocked 피차단자
	 * @return 차단이 존재하면 true, 그렇지 않으면 false
	 */
	public boolean existsByBlockerAndBlocked(User blocker, User blocked) {
		return userBlockRepository.existsByBlockerIdAndBlockeeId(blocker.getId(), blocked.getId());
	}

	/**
	 * 차단당한 유저의 아이디를 가져오는 메서드
	 * @param blocker 차단자
	 * @return 피차단자 id Set
	 */
	public Set<String> findBlockeeUserIdsByBlocker(User blocker) {
		return userBlockRepository.findBlockeeIdsByBlockerUserId(blocker.getId());
	}

	/**
	 * 차단을 한 유저의 아이디를 가져오는 메서드
	 * @param blockee 차단당한 자
	 * @return 차단자 id Set
	 */
	public Set<String> findBlockerUserIdsByBlockee(User blockee) {
		return userBlockRepository.findBlockerIdsByBlockeeUserId(blockee.getId());
	}

	/**
	 * 피차단자 ID 목록으로 차단자 ID 목록을 조회하는 메서드
	 * @param blockeeUserIds 차단당한 자들의 아이디 Set
	 * @return 차단자 id Set
	 */
	public Set<String> findBlockerUserIdsByUserIds(@NotEmpty Set<String> blockeeUserIds) {
		return userBlockRepository.findBlockerIdsByBlockeeUserIds(blockeeUserIds);
	}
}
