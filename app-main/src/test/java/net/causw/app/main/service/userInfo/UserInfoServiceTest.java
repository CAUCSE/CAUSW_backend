package net.causw.app.main.service.userInfo;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.userInfo.UserInfoSearchConditionDto;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.repository.userInfo.query.UserInfoQueryRepository;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserInfoService 테스트")
class UserInfoServiceTest {

	@InjectMocks
	private UserInfoService userInfoService;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private UserInfoQueryRepository userInfoQueryRepository;

	@Nested
	@DisplayName("getUserInfoByUser 테스트")
	class GetUserInfoByUserTest {

		private User testUser;
		private UserInfo testUserInfo;

		@BeforeEach
		void setup() {
			testUser = mock(User.class);
			given(testUser.getId()).willReturn("testUserId");
			testUserInfo = mock(UserInfo.class);
		}

		@Test
		@DisplayName("성공: 유저 동문수첩 정보 조회")
		void getUserInfoByUser_Success() {
			// given
			given(userInfoRepository.findByUserId(testUser.getId()))
				.willReturn(Optional.of(testUserInfo));

			// when
			UserInfo result = userInfoService.getUserInfoByUser(testUser);

			// then
			assertThat(result).isNotNull();
			assertThat(result).isEqualTo(testUserInfo);
			verify(userInfoRepository, times(1)).findByUserId(testUser.getId());
		}

		@Test
		@DisplayName("실패: 유저 동문수첩 정보가 없는 경우 예외 발생")
		void getUserInfoByUser_ThrowsException_WhenUserInfoNotFound() {
			// given
			given(userInfoRepository.findByUserId(testUser.getId()))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userInfoService.getUserInfoByUser(testUser))
				.isInstanceOf(BadRequestException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROW_DOES_NOT_EXIST);

			verify(userInfoRepository, times(1)).findByUserId(testUser.getId());
		}
	}

	@Nested
	@DisplayName("searchUserInfo 테스트")
	class SearchUserInfoTest {

		@Test
		@DisplayName("성공: 검색 조건에 따른 동문수첩 페이징 조회")
		void searchUserInfo_Success() {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			UserInfoSearchConditionDto condition = new UserInfoSearchConditionDto(
				"keyword", 2000, 2020, null
			);

			UserInfo testUserInfo = mock(UserInfo.class);
			Page<UserInfo> expectedPage = new PageImpl<>(List.of(testUserInfo), pageable, 1);

			given(userInfoQueryRepository.searchUserInfo(condition, pageable))
				.willReturn(expectedPage);

			// when
			Page<UserInfo> result = userInfoService.searchUserInfo(pageable, condition);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getTotalElements()).isEqualTo(1);
			verify(userInfoQueryRepository, times(1)).searchUserInfo(condition, pageable);
		}
	}

	@Nested
	@DisplayName("getOrCreateUserInfoFromUser 테스트")
	class GetOrCreateUserInfoFromUserTest {

		private User testUser;
		private UserInfo testUserInfo;

		@BeforeEach
		void setup() {
			testUser = mock(User.class);
			given(testUser.getId()).willReturn("testUserId");
			testUserInfo = mock(UserInfo.class);
		}

		@Test
		@DisplayName("성공: 기존 동문수첩 정보 반환")
		void getOrCreateUserInfoFromUser_ReturnsExistingUserInfo() {
			// given
			given(userInfoRepository.findByUserId(testUser.getId()))
				.willReturn(Optional.of(testUserInfo));

			// when
			UserInfo result = userInfoService.getOrCreateUserInfoFromUser(testUser);

			// then
			assertThat(result).isNotNull();
			assertThat(result).isEqualTo(testUserInfo);
			verify(userInfoRepository, times(1)).findByUserId(testUser.getId());
			verify(userInfoRepository, never()).save(any(UserInfo.class));
		}

		@Test
		@DisplayName("성공: 동문수첩 정보가 없으면 새로 생성")
		void getOrCreateUserInfoFromUser_CreatesNewUserInfo_WhenNotExists() {
			// given
			UserInfo newUserInfo = mock(UserInfo.class);

			given(userInfoRepository.findByUserId(testUser.getId()))
				.willReturn(Optional.empty());
			given(userInfoRepository.save(any(UserInfo.class)))
				.willReturn(newUserInfo);

			// when
			UserInfo result = userInfoService.getOrCreateUserInfoFromUser(testUser);

			// then
			assertThat(result).isNotNull();
			assertThat(result).isEqualTo(newUserInfo);
			verify(userInfoRepository, times(1)).findByUserId(testUser.getId());
			verify(userInfoRepository, times(1)).save(any(UserInfo.class));
		}
	}
}