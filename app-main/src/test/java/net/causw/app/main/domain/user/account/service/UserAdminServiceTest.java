package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserDetailItem;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@Mock
	private UserReader userReader;

	@InjectMocks
	private UserAdminService userAdminService;

	/* =========================
	 * 유저 목록 조회
	 * ========================= */
	@Test
	@DisplayName("유저 목록 조회 조건이 주어지면 페이징된 유저 목록을 반환한다")
	void givenUserListCondition_whenGetUserList_thenReturnPagedUserList() {
		// given
		UserListCondition condition = new UserListCondition(
			"홍길동",
			UserState.ACTIVE,
			AcademicStatus.ENROLLED,
			Department.SCHOOL_OF_SW);

		Pageable pageable = PageRequest.of(0, 10);

		User user1 = ObjectFixtures.getCertifiedUserWithId("user-1");
		User user2 = ObjectFixtures.getCertifiedUserWithId("user-2");

		Page<User> users = new PageImpl<>(
			List.of(user1, user2),
			pageable,
			2);

		when(userReader.findUserList(any(UserListCondition.class), any(Pageable.class)))
			.thenReturn(users);

		// when
		Page<UserListItem> result = userAdminService.getUserList(condition, pageable);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2)
			.extracting(UserListItem::name)
			.containsExactly("name", "name");

		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getNumber()).isEqualTo(0);

		verify(userReader).findUserList(condition, pageable);
	}

	/* =========================
	 * 유저 상세 조회
	 * ========================= */
	@Nested
	@DisplayName("유저 상세 조회")
	class GetUserDetail {

		@Test
		@DisplayName("사용자가 존재하면 사용자 상세 정보를 반환한다")
		void givenValidUserId_whenGetUserDetail_thenReturnUserDetail() {
			// given
			String userId = "user-1";
			User user = ObjectFixtures.getCertifiedUserWithId(userId);

			when(userReader.findDetailById(userId)).thenReturn(user);

			// when
			UserDetailItem result = userAdminService.getUserDetail(userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo(userId);
			assertThat(result.email()).isEqualTo(user.getEmail());
			assertThat(result.name()).isEqualTo(user.getName());

			verify(userReader).findDetailById(userId);
		}

		@Test
		@DisplayName("존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
		void givenInvalidUserId_whenGetUserDetail_thenThrowUserNotFound() {
			// given
			String invalidUserId = "invalid-user-id";

			when(userReader.findDetailById(invalidUserId))
				.thenThrow(UserErrorCode.USER_NOT_FOUND.toBaseException());

			// when
			Throwable throwable = catchThrowable(
				() -> userAdminService.getUserDetail(invalidUserId));

			// then
			assertThat(throwable)
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

			verify(userReader).findDetailById(invalidUserId);
		}
	}
}
