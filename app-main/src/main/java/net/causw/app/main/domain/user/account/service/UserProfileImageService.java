package net.causw.app.main.domain.user.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.repository.UserProfileImageRepository;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;
import net.causw.app.main.domain.user.account.api.v2.dto.response.ProfileImageResponse;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileImageService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final UuidFileService uuidFileService;
	private final UserProfileImageRepository userProfileImageRepository;

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
		if (profileImageType == ProfileImageType.CUSTOM) {
			throw UserErrorCode.INVALID_PROFILE_IMAGE_TYPE.toBaseException();
		}

		User user = userReader.findUserById(userId);
		UserProfileImage existingProfileImage = user.getUserProfileImage();

		// 먼저 User의 userProfileImage 참조를 null로 설정
		user.updateProfileImageToDefault(profileImageType);
		userWriter.save(user);

		// 기존 커스텀 이미지 삭제 (User save 이후 수행)
		if (existingProfileImage != null) {
			UuidFile oldUuidFile = existingProfileImage.getUuidFile();
			userProfileImageRepository.delete(existingProfileImage);
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
		UserProfileImage existingProfileImage = user.getUserProfileImage();

		// 새 파일 먼저 업로드
		UuidFile newUuidFile = uuidFileService.saveFile(imageFile, FilePath.USER_PROFILE);

		if (existingProfileImage != null) {
			UuidFile oldUuidFile = existingProfileImage.getUuidFile();

			// 기존 UserProfileImage에 새 파일 연결 후 User에 반영
			existingProfileImage.setUuidFile(newUuidFile);
			user.updateProfileImageToCustom(existingProfileImage);
			userWriter.save(user);

			// 기존 파일 삭제 (User save 이후 수행)
			if (oldUuidFile != null) {
				uuidFileService.deleteFile(oldUuidFile.getId());
			}
		} else {
			// 새 UserProfileImage 생성 및 User에 연결
			UserProfileImage newProfileImage = UserProfileImage.of(user, newUuidFile);
			user.updateProfileImageToCustom(newProfileImage);
			userWriter.save(user);
		}

		return ProfileImageResponse.of(ProfileImageType.CUSTOM, user.getProfileUrl());
	}
}
