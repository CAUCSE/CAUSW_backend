package net.causw.app.main.service.userInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.List;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserCareer;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.user.UserUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserCareerDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.repository.userInfo.UserCareerRepository;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.user.UserService;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

  @InjectMocks
  private UserInfoService userInfoService;

  @Mock
  private UserInfoRepository userInfoRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserCareerRepository userCareerRepository;

  @Mock
  private UserService userService;

  @Mock
  private PageableFactory pageableFactory;

  @Mock
  private MultipartFile profileImage;

  private final User certifiedUser = ObjectFixtures.getCertifiedUserWithId("userId");
  private final UserInfo userInfo = UserInfo.of(certifiedUser);
  private final UserCareerDto careerDto =  UserCareerDto.builder()
      .startYear(2023).startMonth(1).endYear(2024).endMonth(1)
      .description("CAU Company Backend Developer")
      .build();
  
  private final UserCareerDto currentCareerDto = UserCareerDto.builder()
      .startYear(2024).startMonth(2).endYear(null).endMonth(null)
      .description("Current Company Frontend Developer")
      .build();
  private final UserCareer userCareer = UserCareer.of(
      userInfo,
      careerDto.getStartYear(), careerDto.getStartMonth(),
      careerDto.getEndYear(), careerDto.getEndMonth(),
      careerDto.getDescription());
  private final UserInfoUpdateRequestDto updateRequestDto = UserInfoUpdateRequestDto.builder()
      .phoneNumber("010-1234-5678")
      .job("백엔드 개발자")
      .userCareer(List.of(careerDto))
      .isPhoneNumberVisible(true)
      .build();



  @Nested
  class UpdateTest {

    @BeforeEach
    public void setup() {
      given(userRepository.findById(certifiedUser.getId())).willReturn(Optional.of(certifiedUser));
    }

    @Test
    @DisplayName("상세정보 생성 성공")
    void createUserInfoSuccess() {
      // given
      given(userCareerRepository.save(any(UserCareer.class))).willReturn(userCareer);
      given(userInfoRepository.findByUserId(certifiedUser.getId())).willReturn(Optional.of(userInfo));

      // when
      UserInfoResponseDto result = userInfoService.update(certifiedUser.getId(), updateRequestDto, profileImage);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(certifiedUser.getId());
      verify(userService).update(eq(certifiedUser), any(UserUpdateRequestDto.class), eq(profileImage));
      verify(userCareerRepository).save(any(UserCareer.class));
    }

    @Test
    @DisplayName("상세정보 삭제 성공")
    void deleteUserInfoSuccess() {
      // given
      UserInfoUpdateRequestDto updateRequestDto = UserInfoUpdateRequestDto.builder()
          .userCareer(List.of())
          .isPhoneNumberVisible(true)
          .build();
      UserCareer userCareer = UserCareer.of(
          userInfo,
          careerDto.getStartYear(), careerDto.getStartYear(),
          careerDto.getEndYear(),careerDto.getEndYear(),
          careerDto.getDescription()
      );

      given(userInfoRepository.findByUserId(certifiedUser.getId())).willReturn(Optional.of(userInfo));
      given(userCareerRepository.findAllCareerByUserInfoId(userInfo.getId())).willReturn(List.of(userCareer));

      // when
      UserInfoResponseDto result = userInfoService.update(certifiedUser.getId(), updateRequestDto, profileImage);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(certifiedUser.getId());
      verify(userCareerRepository).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("커리어 날짜가 유효하지 않으면 예외 발생")
    void invalidCareerDateThrowsException() {
      // given
      UserCareerDto invalidCareer = UserCareerDto.builder()
          .startYear(2025).startMonth(5).endYear(2024).endMonth(4)
          .description("Invalid career")
          .build();

      UserInfoUpdateRequestDto updateRequestDto = UserInfoUpdateRequestDto.builder()
          .userCareer(List.of(invalidCareer))
          .isPhoneNumberVisible(true)
          .build();

      given(userInfoRepository.findByUserId(certifiedUser.getId())).willReturn(Optional.of(userInfo));

      // when & then
      assertThatThrownBy(() -> userInfoService.update(certifiedUser.getId(), updateRequestDto, profileImage))
          .isInstanceOf(BadRequestException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("사용자 정보 갱신 실패시 세부정보 갱신도 함께 실패")
    void userServiceUpdateFailedThenUserInfoServiceUpdateFailed() {
      // given
      doThrow(new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.NICKNAME_ALREADY_EXIST))
          .when(userService).update(eq(certifiedUser), any(UserUpdateRequestDto.class), eq(profileImage));

      // when & then
      assertThatThrownBy(() -> userInfoService.update(certifiedUser.getId(), updateRequestDto, profileImage))
          .isInstanceOf(BadRequestException.class)
          .extracting("errorCode").isEqualTo(ErrorCode.ROW_ALREADY_EXIST);
    }
  }
}
