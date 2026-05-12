package net.causw.app.main.domain.asset.file.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.repository.UserProfileImageRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 유저 프로필 이미지 쓰기 전담 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Transactional
public class UserProfileImageWriter {

	private final UserProfileImageRepository userProfileImageRepository;

	/**
	 * 프로필 이미지를 저장합니다.
	 *
	 * @param userProfileImage 저장할 프로필 이미지 엔티티
	 * @return 저장된 프로필 이미지 엔티티
	 */
	public UserProfileImage save(UserProfileImage userProfileImage) {
		return userProfileImageRepository.save(userProfileImage);
	}

	/**
	 * 유저와 파일로 새 프로필 이미지를 생성하여 저장합니다.
	 *
	 * @param user     프로필 이미지의 소유 유저
	 * @param uuidFile 저장할 파일 엔티티
	 * @return 저장된 프로필 이미지 엔티티
	 */
	public UserProfileImage saveNew(User user, UuidFile uuidFile) {
		return userProfileImageRepository.save(UserProfileImage.of(user, uuidFile));
	}

	/**
	 * 프로필 이미지 엔티티를 삭제합니다.
	 *
	 * @param userProfileImage 삭제할 프로필 이미지 엔티티
	 */
	public void delete(UserProfileImage userProfileImage) {
		userProfileImageRepository.delete(userProfileImage);
	}
}
