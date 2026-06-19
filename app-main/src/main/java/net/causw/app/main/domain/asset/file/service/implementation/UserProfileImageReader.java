package net.causw.app.main.domain.asset.file.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.repository.UserProfileImageRepository;

import lombok.RequiredArgsConstructor;

/**
 * 유저 프로필 이미지 조회 전담 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileImageReader {

	private final UserProfileImageRepository userProfileImageRepository;

	/**
	 * 유저 ID로 프로필 이미지를 조회합니다.
	 *
	 * @param userId 유저 ID
	 * @return 프로필 이미지 Optional
	 */
	public Optional<UserProfileImage> findByUserId(String userId) {
		return userProfileImageRepository.findByUserId(userId);
	}

	/**
	 * 유저 ID로 프로필 이미지를 조회합니다. 없으면 null을 반환합니다.
	 *
	 * @param userId 유저 ID
	 * @return 프로필 이미지 또는 null
	 */
	public UserProfileImage findByUserIdOrNull(String userId) {
		return userProfileImageRepository.findByUserId(userId).orElse(null);
	}

	/**
	 * 유저 ID 목록으로 프로필 이미지를 일괄 조회하여 Map으로 반환합니다.
	 *
	 * @param userIds 유저 ID 목록
	 * @return userId → UserProfileImage Map
	 */
	public Map<String, UserProfileImage> findMapByUserIds(List<String> userIds) {
		return userProfileImageRepository.findByUserIdIn(userIds)
			.stream()
			.collect(Collectors.toMap(upi -> upi.getUser().getId(), Function.identity()));
	}
}
