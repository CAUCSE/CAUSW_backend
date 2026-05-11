package net.causw.app.main.domain.user.account.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageWriter;
import net.causw.app.main.domain.user.account.api.v2.dto.response.ProfileImageResponse;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.domain.user.account.event.UserWithdrawalEvent;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileImageService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UuidFileService uuidFileService;
	private final UserProfileImageReader userProfileImageReader;
	private final UserProfileImageWriter userProfileImageWriter;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 프로필 이미지를 기본 이미지로 변경합니다.
	 * <p>
	 * 기존에 커스텀 이미지가 있는 경우 기존 이미지 파일과 UserProfileImage 엔티티를 삭제합니다.
	 * </p>
	 *
	 * @param userId          변경할 유저 ID
	 * @param profileImageType 기본 이미지 타입 (MALE_1, MALE_2, FEMALE_1, FEMALE_2)
	 * @return 변경된 프로필 이미지 정보
	 */
	@Transactional
	public ProfileImageResponse updateToDefaultProfileImage(String userId, ProfileImageType profileImageType) {
		if (profileImageType == ProfileImageType.CUSTOM || profileImageType == ProfileImageType.GHOST) {
			throw UserErrorCode.INVALID_PROFILE_IMAGE_TYPE.toBaseException();
		}

		User user = userReader.findUserById(userId);
		UserProfileImage existingProfileImage = userProfileImageReader.findByUserIdOrNull(userId);

		user.updateProfileImageToDefault(profileImageType);
		userWriter.save(user);

		// 기존 커스텀 이미지 삭제 (User save 이후 수행)
		if (existingProfileImage != null) {
			UuidFile oldUuidFile = existingProfileImage.getUuidFile();
			userProfileImageWriter.delete(existingProfileImage);
			if (oldUuidFile != null) {
				uuidFileService.deleteFile(oldUuidFile.getId());
			}
		}

		return ProfileImageResponse.of(profileImageType, null);
	}

	/**
	 * 프로필 이미지를 커스텀 이미지로 변경합니다.
	 * <p>
	 * 기존에 커스텀 이미지가 있는 경우 기존 이미지 파일을 삭제하고 새 이미지로 교체합니다.
	 * </p>
	 *
	 * @param userId    변경할 유저 ID
	 * @param imageFile 업로드할 커스텀 이미지 파일
	 * @return 변경된 프로필 이미지 정보
	 */
	@Transactional
	public ProfileImageResponse updateToCustomProfileImage(String userId, MultipartFile imageFile) {
		User user = userReader.findUserById(userId);
		UserProfileImage existingProfileImage = userProfileImageReader.findByUserIdOrNull(userId);

		// 새 파일 먼저 업로드
		UuidFile newUuidFile = uuidFileService.saveFile(imageFile, FilePath.USER_PROFILE);

		if (existingProfileImage != null) {
			UuidFile oldUuidFile = existingProfileImage.getUuidFile();

			// 기존 UserProfileImage에 새 파일 연결
			existingProfileImage.setUuidFile(newUuidFile);
			userProfileImageWriter.save(existingProfileImage);
			user.updateProfileImageToCustom();
			userWriter.save(user);

			// 기존 파일 삭제 (User save 이후 수행)
			if (oldUuidFile != null) {
				uuidFileService.deleteFile(oldUuidFile.getId());
			}
		} else {
			// 새 UserProfileImage 생성 및 저장
			userProfileImageWriter.saveNew(user, newUuidFile);
			user.updateProfileImageToCustom();
			userWriter.save(user);
		}

		return ProfileImageResponse.of(ProfileImageType.CUSTOM, newUuidFile.getFileUrl());
	}

	/**
	 * 탈퇴 처리 시 커스텀 프로필 이미지 파일 삭제를 요청합니다.
	 * <p>
	 * 프로필 이미지 연결 정보와 파일 메타데이터는 D+30 배치에서 최종 삭제합니다.
	 * </p>
	 *
	 * @param userId 탈퇴 처리할 유저 ID
	 */
	public void requestProfileImageDeletionForWithdrawal(String userId) {
		userProfileImageReader.findByUserId(userId).ifPresent(profileImage -> {
			UuidFile uuidFile = uuidFileService.findUuidFileById(profileImage.getUuidFile().getId());

			// S3 Key만 추출해서 이벤트 발행 (실시간 S3 삭제 시도용)
			applicationEventPublisher.publishEvent(new UserWithdrawalEvent(uuidFile.getFileKey()));
		});
	}

	/**
	 * 탈퇴 후 30일이 지난 유저들의 프로필 이미지 정보를 최종 삭제합니다.
	 * <p>
	 * 커스텀 프로필 이미지 파일, 파일 메타데이터, 프로필 이미지 연결 정보를 삭제합니다.
	 * 각 유저별 삭제 실패는 로그로 기록하고 다음 유저 처리를 계속 진행합니다.
	 * </p>
	 *
	 * @param users 프로필 이미지 정보를 정리할 탈퇴 후 30일 경과 유저 목록
	 */
	public void cleanupProfileImagesForBatch(List<User> users) {
		for (User user : users) {
			userProfileImageReader.findByUserId(user.getId()).ifPresent(profileImage -> {
				try {
					uuidFileService.deleteFile(profileImage.getUuidFile().getId());
					userProfileImageWriter.delete(profileImage);
				} catch (Exception e) {
					log.error("[유저 정리 배치] 실패. UserID: {}", user.getId(), e);
				}
			});
		}
	}
}
