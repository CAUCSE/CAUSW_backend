package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoListCondition;
import net.causw.app.main.domain.user.account.service.dto.request.UserInfoUpdateCommand;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoDetailResult;
import net.causw.app.main.domain.user.account.service.dto.result.UserInfoSummaryResult;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoCreator;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoReader;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.mapper.UserInfoMapper;
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
	private UserInfoMapper userInfoMapper;

	@Mock
	private PageableFactory pageableFactory;

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
			UserInfoDetailResult resultDto = ObjectFixtures.detailResult();

			when(userInfoReader.findById(userInfoId)).thenReturn(Optional.of(userInfo));
			when(userInfoMapper.toUserInfoDetailResult(userInfo)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getDetailUserInfo(userInfoId);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findById(userInfoId);
			verify(userInfoMapper).toUserInfoDetailResult(userInfo);
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
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserInfoErrorCode.USERINFO_NOT_FOUND);

			verify(userInfoReader).findById(userInfoId);
			verify(userInfoMapper, never()).toUserInfoDetailResult(any());
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
			User user = ObjectFixtures.user();
			when(user.getId()).thenReturn(userId);

			UserInfo userInfo = ObjectFixtures.userInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.detailResult();

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(userInfo));
			when(userInfoMapper.toMyUserInfoDetailResult(userInfo)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getMyDetailUserInfo(user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator, never()).createAndSave(any(User.class));
			verify(userInfoMapper).toMyUserInfoDetailResult(userInfo);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성")
		void givenNoUserInfo_whenGetMyDetailUserInfo_thenCreatesAndReturnsMyDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.user();
			when(user.getId()).thenReturn(userId);

			UserInfo created = ObjectFixtures.userInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.detailResult();

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(userInfoCreator.createAndSave(user)).thenReturn(created);
			when(userInfoMapper.toMyUserInfoDetailResult(created)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getMyDetailUserInfo(user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator).createAndSave(user);
			verify(userInfoMapper).toMyUserInfoDetailResult(created);
		}
	}

	@Nested
	@DisplayName("내 동문 수첩 프로필 수정")
	class UpdateUserInfo {

		@Test
		@DisplayName("이미 내 동문 수첩 프로필이 존재하면 새로 생성하지 않고 업데이트")
		void givenExistingUserInfo_whenUpdateUserInfo_thenReturnsDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.user();
			when(user.getId()).thenReturn(userId);

			UserInfoUpdateCommand request = ObjectFixtures.updateRequest();

			UserInfo existing = ObjectFixtures.userInfo();
			UserInfo updated = ObjectFixtures.userInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.detailResult();

			@SuppressWarnings("unchecked") Set<String> userTechStack = mock(Set.class);
			@SuppressWarnings("unchecked") Set<String> userInterestTech = mock(Set.class);
			@SuppressWarnings("unchecked") Set<String> userInterestDomain = mock(Set.class);

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(existing));
			when(userInfoWriter.save(existing)).thenReturn(updated);
			when(userInfoMapper.toUserInfoDetailResult(updated)).thenReturn(resultDto);

			doNothing().when(existing).update(any(), any(), any(), anyBoolean());
			when(existing.getUserTechStack()).thenReturn(userTechStack);
			when(existing.getUserInterestTech()).thenReturn(userInterestTech);
			when(existing.getUserInterestDomain()).thenReturn(userInterestDomain);

			// when
			UserInfoDetailResult result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator, never()).createAndSave(any(User.class));
			verify(existing).update(null, null, List.of(), false);
			verify(userTechStack).clear();
			verify(userInterestTech).clear();
			verify(userInterestDomain).clear();
			verify(userInfoWriter).save(existing);
			verify(userInfoMapper).toUserInfoDetailResult(updated);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성 후 업데이트")
		void givenNoUserInfo_whenUpdateUserInfo_thenCreatesAndReturnsDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.user();
			when(user.getId()).thenReturn(userId);

			UserInfoUpdateCommand request = ObjectFixtures.updateRequest();

			UserInfo created = ObjectFixtures.userInfo();
			UserInfo updated = ObjectFixtures.userInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.detailResult();

			@SuppressWarnings("unchecked") Set<String> userTechStack = mock(Set.class);
			@SuppressWarnings("unchecked") Set<String> userInterestTech = mock(Set.class);
			@SuppressWarnings("unchecked") Set<String> userInterestDomain = mock(Set.class);

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(userInfoCreator.createAndSave(user)).thenReturn(created);
			when(userInfoWriter.save(created)).thenReturn(updated);
			when(userInfoMapper.toUserInfoDetailResult(updated)).thenReturn(resultDto);

			doNothing().when(created).update(any(), any(), any(), anyBoolean());
			when(created.getUserTechStack()).thenReturn(userTechStack);
			when(created.getUserInterestTech()).thenReturn(userInterestTech);
			when(created.getUserInterestDomain()).thenReturn(userInterestDomain);

			// when
			UserInfoDetailResult result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator).createAndSave(user);
			verify(created).update(null, null, List.of(), false);
			verify(userTechStack).clear();
			verify(userInterestTech).clear();
			verify(userInterestDomain).clear();
			verify(userInfoWriter).save(created);
			verify(userInfoMapper).toUserInfoDetailResult(updated);
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

			UserInfoSummaryResult s1 = ObjectFixtures.summaryResult();
			UserInfoSummaryResult s2 = ObjectFixtures.summaryResult();

			when(userInfoReader.findUserInfoWithFilter(condition, pageable)).thenReturn(page);
			when(userInfoMapper.toUserInfoSummaryResult(u1)).thenReturn(s1);
			when(userInfoMapper.toUserInfoSummaryResult(u2)).thenReturn(s2);

			// when
			Page<UserInfoSummaryResult> result = userInfoService.getUserInfoPage(condition, pageNum);

			// then
			assertThat(result.getContent()).containsExactly(s1, s2);

			verify(pageableFactory).create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
			verify(userInfoReader).findUserInfoWithFilter(condition, pageable);
			verify(userInfoMapper).toUserInfoSummaryResult(u1);
			verify(userInfoMapper).toUserInfoSummaryResult(u2);
		}
	}

	static class ObjectFixtures {
		static User user() {
			return mock(User.class);
		}

		static UserInfo userInfo() {
			return mock(UserInfo.class);
		}

		static UserInfoUpdateCommand updateRequest() {
			UserInfoUpdateCommand command = mock(UserInfoUpdateCommand.class);
			when(command.description()).thenReturn(null);
			when(command.job()).thenReturn(null);
			when(command.socialLinks()).thenReturn(null);
			when(command.isPhoneNumberVisible()).thenReturn(false);
			when(command.userCareer()).thenReturn(null);
			when(command.userProject()).thenReturn(null);
			when(command.userTechStack()).thenReturn(null);
			when(command.userInterestTech()).thenReturn(null);
			when(command.userInterestDomain()).thenReturn(null);
			return command;
		}

		static UserInfoDetailResult detailResult() {
			return mock(UserInfoDetailResult.class);
		}

		static UserInfoSummaryResult summaryResult() {
			return mock(UserInfoSummaryResult.class);
		}

		static UserInfoListCondition listCondition() {
			return mock(UserInfoListCondition.class);
		}

		static Pageable pageable() {
			return mock(Pageable.class);
		}
	}
}