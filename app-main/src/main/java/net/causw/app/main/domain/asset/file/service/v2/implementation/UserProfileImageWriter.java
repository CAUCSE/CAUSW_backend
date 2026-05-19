package net.causw.app.main.domain.asset.file.service.v2.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.repository.UserProfileImageRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.event.UserProfileImageDeletionRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 프로필 이미지 쓰기 전담 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserProfileImageWriter {

	private final UserProfileImageRepository userProfileImageRepository;
	private final UserProfileImageReader userProfileImageReader;
	private final ApplicationEventPublisher applicationEventPublisher;

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

	/**
	 * 탈퇴 처리 시 커스텀 프로필 이미지 파일 삭제를 요청합니다.
	 * <p>
	 * 프로필 이미지 연결 정보와 파일 메타데이터는 D+30 배치에서 최종 삭제합니다.
	 * </p>
	 *
	 * @param userId 탈퇴 처리할 유저 ID
	 */
	public void requestDeletionForWithdrawal(String userId) {
		userProfileImageReader.findByUserId(userId).ifPresent(profileImage -> {
			UuidFile uuidFile = profileImage.getUuidFile();

			if (uuidFile == null) {
				log.warn("[User Withdraw] 프로필 이미지 파일 정보가 없어 삭제 요청을 건너뜁니다. userId: {}", userId);
				return;
			}

			applicationEventPublisher.publishEvent(
				new UserProfileImageDeletionRequestedEvent(uuidFile.getFileKey()));
		});
	}
}
