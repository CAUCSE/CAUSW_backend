package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
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
import net.causw.app.main.util.ObjectFixtures;
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

	@Mock
	private UserProfileImageReader userProfileImageReader;

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
			User user = ObjectFixtures.getMockUser();
			UserInfo userInfo = ObjectFixtures.getMockUserInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.getMockUserInfoDetailResult();
			UserProfileImage profileImage = null;

			when(userInfoReader.findById(userInfoId)).thenReturn(Optional.of(userInfo));
			when(userInfo.getUser()).thenReturn(user);
			when(user.getId()).thenReturn("user-1");
			when(userProfileImageReader.findByUserIdOrNull("user-1")).thenReturn(profileImage);
			when(userInfoMapper.toDetailResult(userInfo, profileImage)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getDetailUserInfo(userInfoId);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findById(userInfoId);
			verify(userProfileImageReader).findByUserIdOrNull("user-1");
			verify(userInfoMapper).toDetailResult(userInfo, profileImage);
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
			verify(userInfoMapper, never()).toDetailResult(any(), any());
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
			User user = ObjectFixtures.getMockUser();
			when(user.getId()).thenReturn(userId);

			UserInfo userInfo = ObjectFixtures.getMockUserInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.getMockUserInfoDetailResult();
			UserProfileImage profileImage = null;

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(userInfo));
			when(userProfileImageReader.findByUserIdOrNull(userId)).thenReturn(profileImage);
			when(userInfoMapper.toMyDetailResult(userInfo, profileImage)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getMyDetailUserInfo(user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator, never()).createAndSave(any(User.class));
			verify(userProfileImageReader).findByUserIdOrNull(userId);
			verify(userInfoMapper).toMyDetailResult(userInfo, profileImage);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성")
		void givenNoUserInfo_whenGetMyDetailUserInfo_thenCreatesAndReturnsMyDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.getMockUser();
			when(user.getId()).thenReturn(userId);

			UserInfo created = ObjectFixtures.getMockUserInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.getMockUserInfoDetailResult();
			UserProfileImage profileImage = null;

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(userInfoCreator.createAndSave(user)).thenReturn(created);
			when(userProfileImageReader.findByUserIdOrNull(userId)).thenReturn(profileImage);
			when(userInfoMapper.toMyDetailResult(created, profileImage)).thenReturn(resultDto);

			// when
			UserInfoDetailResult result = userInfoService.getMyDetailUserInfo(user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator).createAndSave(user);
			verify(userProfileImageReader).findByUserIdOrNull(userId);
			verify(userInfoMapper).toMyDetailResult(created, profileImage);
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
			User user = ObjectFixtures.getMockUser();
			when(user.getId()).thenReturn(userId);

			UserInfoUpdateCommand request = ObjectFixtures.getUserInfoUpdateCommand();

			UserInfo existing = ObjectFixtures.getMockUserInfo();
			UserInfo updated = ObjectFixtures.getMockUserInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.getMockUserInfoDetailResult();
			UserProfileImage profileImage = null;

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.of(existing));
			when(userInfoWriter.save(existing)).thenReturn(updated);
			when(userProfileImageReader.findByUserIdOrNull(userId)).thenReturn(profileImage);
			when(userInfoMapper.toDetailResult(updated, profileImage)).thenReturn(resultDto);

			doNothing().when(existing).update(any(), any(), anyBoolean());
			doNothing().when(existing).updateSocialLinks(any());
			doNothing().when(existing).updateTechStack(any());
			doNothing().when(existing).updateInterestTech(any());
			doNothing().when(existing).updateInterestDomain(any());

			// when
			UserInfoDetailResult result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator, never()).createAndSave(any(User.class));
			verify(existing).update(null, null, false);
			verify(existing).updateSocialLinks(null);
			verify(existing).updateTechStack(null);
			verify(existing).updateInterestTech(null);
			verify(existing).updateInterestDomain(null);
			verify(userInfoWriter).save(existing);
			verify(userProfileImageReader).findByUserIdOrNull(userId);
			verify(userInfoMapper).toDetailResult(updated, profileImage);
		}

		@Test
		@DisplayName("내 동문 수첩 프로필이 존재하지 않으면 새로 생성 후 업데이트")
		void givenNoUserInfo_whenUpdateUserInfo_thenCreatesAndReturnsDetailDto() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.getMockUser();
			when(user.getId()).thenReturn(userId);

			UserInfoUpdateCommand request = ObjectFixtures.getUserInfoUpdateCommand();

			UserInfo created = ObjectFixtures.getMockUserInfo();
			UserInfo updated = ObjectFixtures.getMockUserInfo();
			UserInfoDetailResult resultDto = ObjectFixtures.getMockUserInfoDetailResult();
			UserProfileImage profileImage = null;

			when(userInfoReader.findByUserId(userId)).thenReturn(Optional.empty());
			when(userInfoCreator.createAndSave(user)).thenReturn(created);
			when(userInfoWriter.save(created)).thenReturn(updated);
			when(userProfileImageReader.findByUserIdOrNull(userId)).thenReturn(profileImage);
			when(userInfoMapper.toDetailResult(updated, profileImage)).thenReturn(resultDto);

			doNothing().when(created).update(any(), any(), anyBoolean());
			doNothing().when(created).updateSocialLinks(any());
			doNothing().when(created).updateTechStack(any());
			doNothing().when(created).updateInterestTech(any());
			doNothing().when(created).updateInterestDomain(any());

			// when
			UserInfoDetailResult result = userInfoService.updateUserInfo(request, user);

			// then
			assertThat(result).isSameAs(resultDto);

			verify(userInfoReader).findByUserId(userId);
			verify(userInfoCreator).createAndSave(user);
			verify(created).update(null, null, false);
			verify(created).updateSocialLinks(null);
			verify(created).updateTechStack(null);
			verify(created).updateInterestTech(null);
			verify(created).updateInterestDomain(null);
			verify(userInfoWriter).save(created);
			verify(userProfileImageReader).findByUserIdOrNull(userId);
			verify(userInfoMapper).toDetailResult(updated, profileImage);
		}
	}

	@Nested
	@DisplayName("동문 수첩 프로필 리스트 조회 및 검색")
	class GetUserInfoPage {

		@Test
		@DisplayName("조건으로 동문 수첩 리스트 조회")
		void givenConditionAndPageNum_whenGetUserInfoPage_thenReturnsSummaryPage() {
			// given
			UserInfoListCondition condition = ObjectFixtures.getMockUserInfoListCondition();
			Integer pageNum = 1;
			String userId = "test-user-id";

			Pageable pageable = ObjectFixtures.getMockPageable();
			when(pageableFactory.create(pageNum, StaticValue.USER_LIST_PAGE_SIZE)).thenReturn(pageable);

			User user1 = ObjectFixtures.getMockUser();
			User user2 = ObjectFixtures.getMockUser();
			when(user1.getId()).thenReturn("user-1");
			when(user2.getId()).thenReturn("user-2");

			UserInfo u1 = ObjectFixtures.getMockUserInfo();
			UserInfo u2 = ObjectFixtures.getMockUserInfo();
			when(u1.getUser()).thenReturn(user1);
			when(u2.getUser()).thenReturn(user2);
			Page<UserInfo> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

			UserInfoSummaryResult s1 = ObjectFixtures.getMockUserInfoSummaryResult();
			UserInfoSummaryResult s2 = ObjectFixtures.getMockUserInfoSummaryResult();

			when(userInfoReader.findUserInfoWithFilter(condition, pageable, userId)).thenReturn(page);
			when(userProfileImageReader.findMapByUserIds(List.of("user-1", "user-2"))).thenReturn(Map.of());
			when(userInfoMapper.toSummaryResult(u2, null)).thenReturn(s2);
			when(userInfoMapper.toSummaryResult(u1, null)).thenReturn(s1);

			// when
			Page<UserInfoSummaryResult> result = userInfoService.getUserInfoPage(condition, pageNum, userId);

			// then
			assertThat(result.getContent()).containsExactly(s1, s2);

			verify(pageableFactory).create(pageNum, StaticValue.USER_LIST_PAGE_SIZE);
			verify(userInfoReader).findUserInfoWithFilter(condition, pageable, userId);
			verify(userProfileImageReader).findMapByUserIds(List.of("user-1", "user-2"));
			verify(userInfoMapper).toSummaryResult(u1, null);
			verify(userInfoMapper).toSummaryResult(u2, null);
		}
	}
}