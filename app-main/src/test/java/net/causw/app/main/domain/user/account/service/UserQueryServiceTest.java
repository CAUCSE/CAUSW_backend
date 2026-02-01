package net.causw.app.main.domain.user.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.dto.request.UserListCondition;
import net.causw.app.main.domain.user.account.service.dto.response.UserListItem;
import net.causw.app.main.util.ObjectFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserReader userReader;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    @DisplayName("유저 목록 조회 조건이 주어지면 페이징된 유저 목록을 반환한다")
    void givenUserListCondition_whenGetUserList_thenReturnPagedUserList() {
        // given
        UserListCondition condition = new UserListCondition(
                "홍길동",
                UserState.ACTIVE,
                AcademicStatus.ENROLLED,
                Department.SCHOOL_OF_SW
        );

        Pageable pageable = PageRequest.of(0, 10);

        User user1 = ObjectFixtures.getCertifiedUserWithId("user-1");
        User user2 = ObjectFixtures.getCertifiedUserWithId("user-2");

        Page<User> users = new PageImpl<>(
                List.of(user1, user2),
                pageable,
                2
        );

        when(userReader.findUserList(any(UserListCondition.class), any(Pageable.class)))
                .thenReturn(users);

        // when
        Page<UserListItem> result = userQueryService.getUserList(condition, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2)
                .extracting(UserListItem::name)
                .containsExactly("name", "name"); // ObjectFixtures 기본 name

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);

        verify(userReader).findUserList(condition, pageable);
    }
}
