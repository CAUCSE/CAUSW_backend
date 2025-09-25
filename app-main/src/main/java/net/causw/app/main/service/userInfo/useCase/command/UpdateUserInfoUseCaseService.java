package net.causw.app.main.service.userInfo.useCase.command;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserCareer;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.user.UserUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserCareerDto;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.repository.userInfo.UserCareerRepository;
import net.causw.app.main.service.user.UserEntityService;
import net.causw.app.main.service.user.UserService;
import net.causw.app.main.service.userInfo.UserInfoService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateUserInfoUseCaseService {

	private final UserEntityService userEntityService;
	private final UserInfoService userInfoService;
	private final UserService userService;
	private final UserCareerRepository userCareerRepository;

	public UserInfoResponseDto execute(String userId, UserInfoUpdateRequestDto request, MultipartFile profileImage) {
		User user = userEntityService.findUserByUserId(userId);

		// 사용자 정보 갱신(전화번호, 프로필 이미지)
		final UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
			.nickname(user.getNickname())
			.phoneNumber(request.getPhoneNumber() == null ? user.getPhoneNumber() : request.getPhoneNumber())
			.build();

		userService.update(user, userUpdateRequestDto, profileImage); // 실패시 user, userInfo 전부 rollback

		// 사용자 상세정보 갱신
		final UserInfo userInfo = userInfoService.upsertUserInfoFromUser(user);

		userInfo.update(
			request.getDescription(), request.getJob(), request.getSocialLinks(), request.getIsPhoneNumberVisible());

		// 사용자 커리어 갱신
		updateUserCareer(request.getUserCareer(), userInfo);

		return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
	}

	protected void updateUserCareer(List<UserCareerDto> userCareerDtoList, UserInfo userInfo) {
		Set<String> requestedIdSet = new HashSet<>();

		for (UserCareerDto userCareerDto : userCareerDtoList) {

			validateUserCareerDate(userCareerDto); // 커리어 시작 시점과 종료 시점 검사

			if (userCareerDto.getId() == null) { // 커리어 생성
				UserCareer userCareer = UserCareer.of(
					userInfo,
					userCareerDto.getStartYear(), userCareerDto.getStartMonth(),
					userCareerDto.getEndYear(), userCareerDto.getEndMonth(),
					userCareerDto.getDescription());

				UserCareer createdCareer = userCareerRepository.save(userCareer);

				requestedIdSet.add(createdCareer.getId()); // 삭제 대상에서 제외

			} else { // 커리어 수정
				UserCareer userCareer = userCareerRepository.findById(userCareerDto.getId())
					.orElseThrow(() -> new BadRequestException(
						ErrorCode.ROW_DOES_NOT_EXIST,
						MessageUtil.USER_CAREER_NOT_FOUND
					));

				userCareer.update(
					userCareerDto.getStartYear(), userCareerDto.getStartMonth(),
					userCareerDto.getEndYear(), userCareerDto.getEndMonth(),
					userCareerDto.getDescription());

				requestedIdSet.add(userCareerDto.getId()); // 삭제 대상에서 제외
			}
		}

		// 커리어 삭제
		List<String> idToDeleteList = userCareerRepository.findAllCareerByUserInfoId(userInfo.getId()).stream()
			.map(BaseEntity::getId)
			.filter(id -> !requestedIdSet.contains(id)).toList();

		if (!idToDeleteList.isEmpty()) {
			userCareerRepository.deleteAllByIdInBatch(idToDeleteList);
		}
	}

	private void validateUserCareerDate(UserCareerDto userCareerDto) {
		int currentYear = LocalDateTime.now().getYear();

		int startMonth = userCareerDto.getStartMonth();
		int startYear = userCareerDto.getStartYear();
		Integer endMonth = userCareerDto.getEndMonth();
		Integer endYear = userCareerDto.getEndYear();

		// 시작일 검증
		boolean isInvalidStartMonth = startMonth < 1 || startMonth > 12;
		boolean isInvalidStartYear = startYear < 1 || startYear > currentYear;

		if (isInvalidStartMonth || isInvalidStartYear) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.INVALID_CAREER_DATE
			);
		}

		// 종료일이 있는 경우에만 검증 (null은 현재 재직 중으로 판단)
		if (endYear != null && endMonth != null) {
			boolean isInvalidEndMonth = endMonth < 1 || endMonth > 12;
			boolean isInvalidEndYear = endYear < 1 || endYear > currentYear;
			boolean isEndBeforeStart = startYear > endYear || (startYear == endYear && startMonth > endMonth);

			if (isInvalidEndMonth || isInvalidEndYear || isEndBeforeStart) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.INVALID_CAREER_DATE
				);
			}
		}
	}
}
