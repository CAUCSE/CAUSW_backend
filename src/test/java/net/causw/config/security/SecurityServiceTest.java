package net.causw.config.security;

import net.causw.application.security.SecurityHelper;
import net.causw.application.security.SecurityService;
import net.causw.domain.aop.annotation.WithMockCustomUser;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.RoleGroup;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class SecurityServiceTest {

    @Spy
    private SecurityService securityService;

    @Nested
    @DisplayName("권한 검사 테스트")
    class RoleTest {

        @Test
        @WithMockCustomUser(roles = Role.ADMIN)
        @DisplayName("사용자가 해당 Role을 가지고 있을 경우 성공")
        void shouldReturnTrue_whenUserHasAdminRole() {
            // when & then
            assertThat(securityService.hasRole("ADMIN")).isTrue();
            assertThat(securityService.hasRole(Role.ADMIN)).isTrue();
        }

        @Test
        @WithMockCustomUser(roles = Role.COMMON)
        @DisplayName("사용자가 해당 Role을 가지고 있지 않는 경우 실패")
        void shouldReturnFalse_whenUserDoesNotHaveAdminRole() {
            // when & then
            assertThat(securityService.hasRole("ADMIN")).isFalse();
            assertThat(securityService.hasRole(Role.ADMIN)).isFalse();
        }

        @Test
        @WithMockCustomUser(roles = Role.ADMIN)
        @DisplayName("사용자의 Role이 해당 RoleGroup 포함될 경우 성공")
        void shouldReturnTrue_whenUserBelongsToRoleGroupExecutives() {
            // when & then
            assertThat(securityService.hasRoleGroup(RoleGroup.EXECUTIVES)).isTrue();
        }

        @Test
        @WithMockCustomUser(roles = Role.COMMON)
        @DisplayName("사용자의 Role이 해당 RoleGroup 포함되지 않는 경우 실패")
        void shouldReturnFalse_whenUserDoesNotBelongToRoleGroupExecutives() {
            // when & then
            assertThat(securityService.hasRoleGroup(RoleGroup.EXECUTIVES)).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 검사 테스트")
    class IsActiveAndNotNoneUserTest {

        @Test
        @WithMockCustomUser(roles = Role.ADMIN, state =  UserState.ACTIVE)
        @DisplayName("사용자 상태가 ACTIVE이고 권한이 NONE이 아닐 경우 성공")
        void shouldReturnTrue_whenUserIsActiveAndRoleIsNotNone() {
            // when & then
            assertThat(securityService.isActiveUser()).isTrue();
        }

        @Test
        @WithMockCustomUser(roles = Role.ADMIN, state =  UserState.AWAIT)
        @DisplayName("사용자 상태가 ACTIVE가 아닌 경우 실패")
        void shouldReturnFalse_whenUserIsNotActiveAndRoleIsValid() {
            // when & then
            assertThat(securityService.isActiveUser()).isFalse();
        }

        @Test
        @WithMockCustomUser(roles = Role.NONE, state =  UserState.ACTIVE)
        @DisplayName("사용자 권한이 NONE일 경우 실패")
        void shouldReturnFalse_whenUserIsActiveButRoleIsNone() {
            // when & then
            assertThat(securityService.isActiveUser()).isFalse();
        }

        @Test
        @WithMockCustomUser(roles = Role.NONE, state =  UserState.AWAIT)
        @DisplayName("사용자 상태가 ACTIVE가 아니고 권한이 NONE일 경우 실패")
        void shouldReturnFalse_whenUserIsNotActiveAndRoleIsNone() {
            // when & then
            assertThat(securityService.isActiveUser()).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 및 학적 검사 테스트")
    class ActiveAndNotNoneUserAndAcademicRecordCertifiedTest {

        @Test
        @WithMockCustomUser(roles = Role.ADMIN, academicStatus = AcademicStatus.UNDETERMINED)
        @DisplayName("권한이 EXECUTIVES_AND_PROFESSOR 소속일 경우 Bypass")
        void shouldReturnTrue_whenUserHasExecutiveRoleRegardlessOfAcademicStatus() {
            // when & then
            when(securityService.isActiveUser()).thenReturn(true);
            assertThat(securityService.isCertifiedUser()).isTrue();
        }

        @Test
        @WithMockCustomUser(roles = Role.COMMON, academicStatus = AcademicStatus.ENROLLED)
        @DisplayName("일반 사용자이고 학적이 인증된 경우 성공")
        void shouldReturnTrue_whenUserIsCommonAndAcademicStatusIsCertified() {
            // when & then
            when(securityService.isActiveUser()).thenReturn(true);
            assertThat(securityService.isCertifiedUser()).isTrue();
        }

        @Test
        @WithMockCustomUser(roles = Role.COMMON, academicStatus = AcademicStatus.UNDETERMINED)
        @DisplayName("일반 사용자이고 학적이 인증되지 않은 경우 실패")
        void shouldReturnFalse_whenUserIsCommonAndAcademicStatusIsUncertified() {
            // when & then
            when(securityService.isActiveUser()).thenReturn(true);
            assertThat(securityService.isCertifiedUser()).isFalse();
        }

        @Test
        @WithMockCustomUser
        @DisplayName("사용자 및 학적 검사 분기 성공")
        void shouldReturnTrue_whenUserIsActiveAndAcademicStatusCertified() {
            try (MockedStatic<SecurityHelper> securityHelperMockedStatic = Mockito.mockStatic(SecurityHelper.class)) {
                when(securityService.isActiveUser()).thenReturn(true);
                securityHelperMockedStatic.when(() -> SecurityHelper.isAcademicRecordCertified(any()))
                        .thenReturn(true);

                assertThat(securityService.isCertifiedUser()).isTrue();
            }
        }

        @Test
        @WithMockCustomUser
        @DisplayName("사용자 및 학적 검사 분기 실패")
        void shouldReturnFalse_whenUserIsActiveButAcademicStatusNotCertified() {
            try (MockedStatic<SecurityHelper> securityHelperMockedStatic = Mockito.mockStatic(SecurityHelper.class)) {
                when(securityService.isActiveUser()).thenReturn(false);
                securityHelperMockedStatic.when(() -> SecurityHelper.isAcademicRecordCertified(any()))
                        .thenReturn(true);

                assertThat(securityService.isCertifiedUser()).isFalse();
            }
        }
    }
}
