package net.causw.application.user;

import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import net.causw.adapter.persistence.repository.user.UserAdmissionRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.excel.UserExcelService;
import net.causw.domain.model.enums.user.UserState;

import net.causw.domain.model.util.ObjectFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  UserService userService;

  @Mock
  UserExcelService userExcelService;

  @Mock
  UserRepository userRepository;

  @Mock
  UserAdmissionRepository userAdmissionRepository;

  @Mock
  HttpServletResponse response;


  @Nested
  class ExportUserListToExcelTest {

    @DisplayName("Excel로 데이터 내보내기 성공 - 가입 대기 유저 목록")
    @Test
    void testExportAwaitUserListToExcelSuccess() {
      //given
      UserState state = UserState.AWAIT;
      String sheetName = state.getDescription() + " 유저";
      UserAdmission userAdmission = ObjectFixtures.getUserAdmission();
      userAdmission.getUser().setState(state);

      given(userAdmissionRepository.findAll()).willReturn(List.of(userAdmission));

      //when
      userService.exportUserListToExcel(response);

      //then
      LinkedHashMap<String, List<UserResponseDto>> exportedUserDataMap = captureGeneratedExcelData();
      List<UserResponseDto> exportedUserList = exportedUserDataMap.get(sheetName);

      verifyUserResponseDto(exportedUserList, state);
    }

    @DisplayName("Excel로 데이터 내보내기 성공 - 활성 유저 목록")
    @Test
    void testExportActiveUserListToExcelSuccess() {
      //given
      UserState state = UserState.ACTIVE;
      String sheetName = state.getDescription() + " 유저";
      User user = ObjectFixtures.getUser();
      user.setState(state);

      given(userRepository.findAllByState(state)).willReturn(List.of(user));

      //when
      userService.exportUserListToExcel(response);

      //then
      LinkedHashMap<String, List<UserResponseDto>> exportedUserDataMap = captureGeneratedExcelData();
      List<UserResponseDto> exportedUserList = exportedUserDataMap.get(sheetName);

      verifyUserResponseDto(exportedUserList, state);
    }

    private LinkedHashMap<String, List<UserResponseDto>> captureGeneratedExcelData() {
      ArgumentCaptor<LinkedHashMap<String, List<UserResponseDto>>> captor =
          ArgumentCaptor.forClass(LinkedHashMap.class);
      verify(userExcelService, times(1))
          .generateExcel(eq(response), anyString(), anyList(), captor.capture());

      return captor.getValue();
    }

    private void verifyUserResponseDto(
        List<UserResponseDto> exportedUserList,
        UserState userState
    ) {
      for (UserResponseDto userResponseDto : exportedUserList) {
        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getState())
            .as("실제 UserResponseDto의 state가 %s이어야 합니다.", userState.getValue())
            .isEqualTo(userState);
      }
    }
  }
}