package net.causw.application.uesrInfo;

import jakarta.transaction.Transactional;
import java.util.Collection;
import net.causw.adapter.persistence.repository.userInfo.UserCareerRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userInfo.UserCareer;
import net.causw.application.dto.user.UserUpdateRequestDto;
import net.causw.application.dto.userInfo.UserCareerResponseDto;
import net.causw.application.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.application.user.UserService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.userInfo.UserInfoRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.userInfo.UserInfo;
import net.causw.application.dto.userInfo.UserInfoResponseDto;
import net.causw.application.dto.userInfo.UsersInfoResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class UserInfoService {
  private final UserRepository userRepository;
  private final UserInfoRepository userInfoRepository;
  private final UserCareerRepository userCareerRepository;
  private final UserService userService;

  public List<UsersInfoResponseDto> getUsersInfo(Integer pageNum) {

    Pageable pageable = PageRequest.of(pageNum, 10);
    Page<UserInfo> userInfoPage = userInfoRepository.findAll(pageable);

    List<UserInfo> userInfoList = userInfoPage.getContent();

    return toUsersInfoResponseDtoList(userInfoList);
  }

  public UserInfoResponseDto getUserInfo(String userId) {

    UserInfo userInfo = findUserInfo(userId);

    return toUserInfoResponseDto(userInfo);
  }

  private List<UsersInfoResponseDto> toUsersInfoResponseDtoList (List<UserInfo> userInfoList) {
    return userInfoList.stream()
        .map(userInfo -> UsersInfoResponseDto.builder()
            .id(userInfo.getId())
            .name(userInfo.getUser().getName())
            .admissionYear(userInfo.getUser().getAdmissionYear())
            .profileImageUrl(userInfo.getUser().getUserProfileImage())
            .major(userInfo.getUser().getMajor())
            .roles(userInfo.getUser().getRoles())
            .academicStatus(userInfo.getUser().getAcademicStatus())
            .description(userInfo.getDescription())
            .job(userInfo.getJob())
            .build()
        ).collect(Collectors.toList());
  }


  private UserInfoResponseDto toUserInfoResponseDto (UserInfo userInfo) {

    List<UserCareer> userCareerList = userInfo.getUserCareer();

    List<UserCareerResponseDto> userCareerResponseDtoList = userCareerList.stream().map(
        career -> UserCareerResponseDto.builder()
            .startYear(career.getStartYear())
            .startMonth(career.getStartMonth())
            .endYear(career.getEndYear())
            .endMonth(career.getEndMonth())
            .description(career.getDescription())
            .build()
    ).collect(Collectors.toList());

    return UserInfoResponseDto.builder()
        .id(userInfo.getId())
        .name(userInfo.getUser().getName())
        .email(userInfo.getUser().getEmail())
        .phoneNumber(userInfo.getUser().getPhoneNumber())
        .admissionYear(userInfo.getUser().getAdmissionYear())
        .major(userInfo.getUser().getMajor())
        .roles(userInfo.getUser().getRoles())
        .career(userCareerResponseDtoList)
        .academicStatus(userInfo.getUser().getAcademicStatus())
        .description(userInfo.getDescription())
        .job(userInfo.getJob())
        .githubLink(userInfo.getGithubLink())
        .linkedInLink(userInfo.getLinkedInLink())
        .instagramLink(userInfo.getInstagramLink())
        .notionLink(userInfo.getNotionLink())
        .velogLink(userInfo.getVelogLink())
        .build();
  }

  public UserInfo findUserInfo(String userId) {

    UserInfo userInfo = userInfoRepository.findByUserId(userId)
        .orElseThrow(() -> new BadRequestException(
            ErrorCode.ROW_DOES_NOT_EXIST,
            MessageUtil.USER_NOT_FOUND
        ));

    return userInfo;
  }

  @Transactional
  public UserInfoResponseDto update(String userId, UserInfoUpdateRequestDto request, MultipartFile profileImage) {

    final UserInfo userInfo = findUserInfo(userId);
    final User user = userInfo.getUser();

    final UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
        .nickname(user.getNickname())
        .phoneNumber(request.getPhoneNumber())
        .build();

    final List<UserCareer> careerList = request.getCareer().stream().map(
        dto -> UserCareer.builder()
            .startYear(dto.getStartYear())
            .startMonth(dto.getStartMonth())
            .endYear(dto.getEndYear())
            .endMonth(dto.getEndMonth())
            .build()
    ).collect(Collectors.toList());

    userService.update(user, userUpdateRequestDto, profileImage);

    userInfo.updateJob(request.getJob());
    userInfo.updateDescription(request.getDescription());
    userInfo.updateUserCareer(careerList);
    userInfo.updateGithubLink(request.getGithubLink());
    userInfo.updateLinkedInLink(request.getLinkedInLink());
    userInfo.updateInstagramLink(request.getInstagramLink());
    userInfo.updateNotionLink(request.getNotionLink());
    userInfo.updateVelogLink(request.getVelogLink());


    return toUserInfoResponseDto(userInfoRepository.save(userInfo));
  }

}
