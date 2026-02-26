package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserInfoUpdateRequestDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoDetailResponseDto;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserInfoSummaryResponseDto;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserInfoDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserInfoErrorCode;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

	@Mock
	private UserInfoCreator userInfoCreator;

	@Mock
	private UserInfoReader userInfoReader;

	@Mock
	private UserInfoDtoMapper userInfoDtoMapper;

	@Mock
	private PageableFactory pageableFactory;

	@Mock
	private UserReader userReader;

	@Mock
	private UserInfoWriter userInfoWriter;

	@InjectMocks
	private UserInfoService userInfoService;

	@Nested
	@DisplayName("동문 수첩 프로필 상세 조회")
	class GetDetailUserInfo {

		@Test
		@DisplayName("동문 수첩 프로필 상세 조회")
		void givenValidUserInfoId_whenGetDetailUserInfo_thenReturnsDetailDto() {
			// given
			String userInfoId = "ui-1";
			UserInfo userInfo = ObjectFixtures.userInfo();
			UserInfoDetailResponseDto response = ObjectFixtures.detailResponse();

			when(userInfoReader.findById(userInfoId)).thenReturn(Optional.of(userInfo));
			when(userInfoDtoMapper.toUserInfoDetailResponseDto(userInfo)).thenReturn(response);

			// when
			UserInfoDetailResponseDto result = userInfoService.getDetailUserInfo(userInfoId);

			// then
			assertThat(result).isSameAs(response);

			verify(userInfoReader).findById(userInfoId);
			verify(userInfoDtoMapper).toUserInfoDetailResponseDto(userInfo);
		}

		@Test
		@DisplayName("동문 수첩 프로필이 존재하지 않을 경우 에러 반환")
		void givenInvalidUserInfoId_whenGetDetailUserInfo_thenThrowsNotFound() {
			// given
			String userInfoId = "ui-not-exist";
			when(userInfoReader.findById(userInfoId)).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userInfoService.getDetailUserInfo(userInfoId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception) e).getErrorCode())
				.isEqualTo(UserInfoErrorCode.USERINFO_NOT_FOUND);

			verify(userInfoReader).findById(userInfoId);
			verify(userInfoDtoMapper, never()).toUserInfoDetailResponseDto(any());
		}
	}

	@Nested
	@DisplayName("내 동문 수첩 프로필 상세 조회")
	class GetMyDetailUserInfo {

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하면 새로 생성하지 않고 상세 조회")
		void givenExistingUserInfo_whenGetMyDetailUserInfo_thenReturnsMyDetailDto() {
			// given
			String userId = "user-1";
			UserInfo userInfo = ObjectFixtures.userInfo();
			UserInfoDetailResponseDto response = ObjectFixtures.detailResponse();

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(userInfo));
			when(userInfoDtoMapper.toMyUserInfoDetailResponseDto(userInfo)).thenReturn(response);

			// when
			UserInfoDetailResponseDto result = userInfoService.getMyDetailUserInfo(userId);

			// then
			assertThat(result).isSameAs(response);

			verify(userInfoReader).findByUserId(userId);
			verify(userReader, never()).findUserById(any());
			verify(userInfoCreator, never()).save(any());
			verify(userInfoDtoMapper).toMyUserInfoDetailResponseDto(userInfo);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성")
		void givenNoUserInfo_whenGetMyDetailUserInfo_thenCreatesAndReturnsMyDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.user();
			UserInfo saved = ObjectFixtures.userInfo();
			UserInfoDetailResponseDto response = ObjectFixtures.detailResponse();

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(userReader.findUserById(userId)).thenReturn(user);
			when(userInfoCreator.save(any(UserInfo.class))).thenReturn(saved);
			when(userInfoDtoMapper.toMyUserInfoDetailResponseDto(saved)).thenReturn(response);

			// when
			UserInfoDetailResponseDto result = userInfoService.getMyDetailUserInfo(userId);

			// then
			assertThat(result).isSameAs(response);

			verify(userInfoReader).findByUserId(userId);
			verify(userReader).findUserById(userId);
			verify(userInfoCreator).save(any(UserInfo.class));
			verify(userInfoDtoMapper).toMyUserInfoDetailResponseDto(saved);
		}
	}

	@Nested
	@DisplayName("내 동문 수첩 프로필 수정")
	class UpdateUserInfo {

		@Test
		@DisplayName("이미 내 동문 수첩 프로필이 존재하면 새로 생성하지 않고 업데이트")
		void givenExistingUserInfo_whenUpdateUserInfo_thenReturnsDetailDto() {
			// given
			User user = ObjectFixtures.user();
			UserInfoUpdateRequestDto request = ObjectFixtures.updateRequest();

			String userId = "user-1";
			when(user.getId()).thenReturn(userId);

			UserInfo existing = ObjectFixtures.userInfo();
			UserInfo updated = ObjectFixtures.userInfo();
			UserInfoDetailResponseDto response = ObjectFixtures.detailResponse();

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(existing));
			when(userInfoWriter.update(eq(request), eq(existing))).thenReturn(updated);
			when(userInfoDtoMapper.toUserInfoDetailResponseDto(updated)).thenReturn(response);

			// when
			UserInfoDetailResponseDto result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(response);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator, never()).save(any());
			verify(userInfoWriter).update(eq(request), eq(existing));
			verify(userInfoDtoMapper).toUserInfoDetailResponseDto(updated);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성")
		void givenNoUserInfo_whenUpdateUserInfo_thenCreatesAndReturnsDetailDto() {
			// given
			User user = ObjectFixtures.user();
			UserInfoUpdateRequestDto request = ObjectFixtures.updateRequest();

			String userId = "user-1";
			when(user.getId()).thenReturn(userId);

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());

			UserInfo saved = ObjectFixtures.userInfo();
			when(userInfoCreator.save(any(UserInfo.class))).thenReturn(saved);

			UserInfo updated = ObjectFixtures.userInfo();
			when(userInfoWriter.update(eq(request), eq(saved))).thenReturn(updated);

			UserInfoDetailResponseDto response = ObjectFixtures.detailResponse();
			when(userInfoDtoMapper.toUserInfoDetailResponseDto(updated)).thenReturn(response);

			// when
			UserInfoDetailResponseDto result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(response);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator).save(any(UserInfo.class));
			verify(userInfoWriter).update(eq(request), eq(saved));
			verify(userInfoDtoMapper).toUserInfoDetailResponseDto(updated);
		}
	}

	@Nested
	@DisplayName("동문 수첩 프로필 리스트 조회 및 검색")
	class GetUserInfoPage {

		@Test
		@DisplayName("조건으로 동문 수첩 리스트 조회")
		void givenConditionAndPageNum_whenGetUserInfoPage_thenReturnsSummaryPage() {
			// given
			UserInfoListCondition condition = ObjectFixtures.listCondition();
			Integer pageNum = 1;

			Pageable pageable = ObjectFixtures.pageable();
			when(pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE)).thenReturn(pageable);

			UserInfo u1 = ObjectFixtures.userInfo();
			UserInfo u2 = ObjectFixtures.userInfo();
			Page<UserInfo> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

			UserInfoSummaryResponseDto s1 = ObjectFixtures.summaryResponse();
			UserInfoSummaryResponseDto s2 = ObjectFixtures.summaryResponse();

			when(userInfoReader.findUserInfoWithFilter(condition, pageable)).thenReturn(page);
			when(userInfoDtoMapper.toUserInfoSummaryResponseDto(u1)).thenReturn(s1);
			when(userInfoDtoMapper.toUserInfoSummaryResponseDto(u2)).thenReturn(s2);

			// when
			Page<UserInfoSummaryResponseDto> result = userInfoService.getUserInfoPage(condition, pageNum);

			// then
			assertThat(result.getContent()).containsExactly(s1, s2);

			verify(pageableFactory).create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
			verify(userInfoReader).findUserInfoWithFilter(condition, pageable);
			verify(userInfoDtoMapper).toUserInfoSummaryResponseDto(u1);
			verify(userInfoDtoMapper).toUserInfoSummaryResponseDto(u2);
		}
	}

	static class ObjectFixtures {
		static User user() {
			return org.mockito.Mockito.mock(User.class);
		}
		static UserInfo userInfo() {
			return org.mockito.Mockito.mock(UserInfo.class);
		}
		static UserInfoUpdateRequestDto updateRequest() {
			return org.mockito.Mockito.mock(UserInfoUpdateRequestDto.class);
		}
		static UserInfoDetailResponseDto detailResponse() {
			return org.mockito.Mockito.mock(UserInfoDetailResponseDto.class);
		}
		static UserInfoSummaryResponseDto summaryResponse() {
			return org.mockito.Mockito.mock(UserInfoSummaryResponseDto.class);
		}
		static UserInfoListCondition listCondition() {
			return org.mockito.Mockito.mock(UserInfoListCondition.class);
		}
		static Pageable pageable() {
			return org.mockito.Mockito.mock(Pageable.class);
		}
	}
}