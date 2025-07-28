package net.causw.app.main.service.userInfo;

import static net.causw.global.constant.StaticValue.DEFAULT_PAGE_SIZE;

import jakarta.transaction.Transactional;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserCareer;
import net.causw.app.main.dto.user.UserUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.user.UserService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.constant.MessageUtil;
import org.springframework.data.domain.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserInfoService {
  private final UserInfoRepository userInfoRepository;
  private final UserService userService;
  private final PageableFactory pageableFactory;

  public Page<UserInfoSummaryResponseDto> getAll(Integer pageNum) {
    return userInfoRepository.findAllByOrderByUpdatedAtDesc(pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE))
        .map(UserDtoMapper.INSTANCE::toUserInfoSummaryResponseDto);
  }

  public UserInfoResponseDto getByUserId(String userId) {
    UserInfo userInfo = userInfoRepository.findByUserId(userId)
        .orElseThrow(() -> new BadRequestException(
            ErrorCode.ROW_DOES_NOT_EXIST,
            MessageUtil.USER_INFO_NOT_FOUND
        ));

    return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
  }

  @Transactional
  public UserInfoResponseDto update(User user, UserInfoUpdateRequestDto request, MultipartFile profileImage) {
    // 사용자 정보 갱신(전화번호, 프로필 이미지)
    final UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
        .nickname(user.getNickname())
        .phoneNumber(request.getPhoneNumber())
        .build();

    userService.update(user, userUpdateRequestDto, profileImage);

    // 사용자 상세정보 갱신 (또는 생성)
    final UserInfo userInfo = userInfoRepository.findByUserId(user.getId())
        .orElse(UserInfo.of(user));

    final List<UserCareer> careerList = request.getUserCareer().stream().map(
        dto -> UserCareer.of(
            userInfo,
            dto.getStartYear(), dto.getStartMonth(), dto.getEndYear(), dto.getEndMonth(), dto.getDescription()
        )).toList();

    userInfo.update(
        request.getDescription(), request.getJob(),
        request.getGithubLink(), request.getLinkedInLink(), request.getInstagramLink(), request.getNotionLink(), request.getVelogLink(),
        careerList,
        request.isPhoneNumberVisible());

    UserInfo updatedUserInfo = userInfoRepository.save(userInfo);

    return UserDtoMapper.INSTANCE.toUserInfoResponseDto(updatedUserInfo);
  }

  public Page<UserInfoSummaryResponseDto> search(final String name, final String job, final String career, final Integer pageNum) {
    return userInfoRepository.findByNameAndJobAndCareer(name, job, career, pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE))
        .map(UserDtoMapper.INSTANCE::toUserInfoSummaryResponseDto);
  }
}
