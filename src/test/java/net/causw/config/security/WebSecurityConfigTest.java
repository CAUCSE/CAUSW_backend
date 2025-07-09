package net.causw.config.security;

import net.causw.adapter.web.DummyController;
import net.causw.application.security.SecurityService;
import net.causw.domain.aop.annotation.WithMockCustomUser;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.causw.config.security.SecurityEndpoints.SecurityEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DummyController.class)
@Import({CustomAuthorizationManager.class, SecurityService.class, WebSecurityConfig.class, CustomAuthenticationEntryPoint.class})
public class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("AuthorizeHttpRequests 테스트")
    class AuthorizeHttpRequestsTest {

        @ParameterizedTest
        @MethodSource("getPublicEndpoints")
        @WithAnonymousUser
        @DisplayName("PUBLIC_ENDPOINTS에 해당할 시 인증 없이 접근 허용")
        void shouldAllowAccess_WhenPublicEndpoint(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointSuccess(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getAuthenticatedEndpoints")
        @WithMockUser
        @DisplayName("AUTHENTICATED_ENDPOINTS에 해당할 시 로그인한 사용자는 접근 허용")
        void shouldAllowAccess_WhenAuthenticatedUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointSuccess(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getAuthenticatedEndpoints")
        @WithAnonymousUser
        @DisplayName("AUTHENTICATED_ENDPOINTS에 해당할 시 비로그인 사용자는 접근 거부")
        void shouldRejectAccess_WhenUnauthenticatedUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointFail(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getActiveUserEndpoints")
        @WithMockCustomUser(roles = Role.COMMON, state = UserState.ACTIVE)
        @DisplayName("ACTIVE_USER_ENDPOINTS에 해당할 시 상태가 ACTIVE인 사용자는 접근 허용")
        void shouldAllowAccess_WhenActiveUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointSuccess(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getActiveUserEndpoints")
        @WithMockCustomUser(roles = Role.COMMON, state = UserState.AWAIT)
        @DisplayName("ACTIVE_USER_ENDPOINTS에 해당할 시 상태가 ACTIVE가 아닌 사용자는 접근 거부")
        void shouldRejectAccess_WhenNotActiveUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointFail(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getCertifiedUserEndpoints")
        @WithMockCustomUser(roles = Role.COMMON, state = UserState.ACTIVE, academicStatus = AcademicStatus.ENROLLED)
        @DisplayName("CERTIFIED_USER_ENDPOINTS에 해당할 시 학적이 정해진 사용자는 접근 허용")
        void shouldAllowAccess_WhenCertifiedUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointSuccess(endpoint);
        }

        @ParameterizedTest
        @MethodSource("getCertifiedUserEndpoints")
        @WithMockCustomUser(roles = Role.COMMON, state = UserState.ACTIVE, academicStatus = AcademicStatus.UNDETERMINED)
        @DisplayName("CERTIFIED_USER_ENDPOINTS에 해당할 시 학적이 미정된 사용자는 접근 거부")
        void shouldRejectAccess_WhenNotCertifiedUser(SecurityEndpoint endpoint) throws Exception {
            testSecurityEndpointFail(endpoint);
        }

        static Stream<SecurityEndpoint> getPublicEndpoints() {
            return Stream.of(SecurityEndpoints.PUBLIC_ENDPOINTS);
        }

        static Stream<SecurityEndpoint> getAuthenticatedEndpoints() {
            return Stream.of(SecurityEndpoints.AUTHENTICATED_ENDPOINTS);
        }

        static Stream<SecurityEndpoint> getActiveUserEndpoints() {
            return Stream.of(SecurityEndpoints.ACTIVE_USER_ENDPOINTS);
        }

        static Stream<SecurityEndpoint> getCertifiedUserEndpoints() {
            return Stream.of(SecurityEndpoints.CERTIFIED_USER_ENDPOINTS);
        }

        private void testSecurityEndpointSuccess(SecurityEndpoint endpoint) throws Exception {
            for (HttpMethod httpMethod : getHttpMethods(endpoint)) {
                String dummyUrl = buildDummyUrl(endpoint);
                mockMvc.perform(MockMvcRequestBuilders.request(httpMethod, dummyUrl))
                        .andExpect(status().isNotFound());
            }
        }

        private void testSecurityEndpointFail(SecurityEndpoint endpoint) throws Exception {
            for (HttpMethod httpMethod : getHttpMethods(endpoint)) {
                String dummyUrl = buildDummyUrl(endpoint);
                mockMvc.perform(MockMvcRequestBuilders.request(httpMethod, dummyUrl))
                        .andExpect(result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isNotEqualTo(HttpStatus.NOT_FOUND.value());
                        });
            }
        }

        private HttpMethod[] getHttpMethods(SecurityEndpoint endpoint) {
            return endpoint.httpMethod() == null ? getAllHttpMethodsWithoutRegistered(endpoint.pattern())
                    : new HttpMethod[]{endpoint.httpMethod()};
        }

        private HttpMethod[] getAllHttpMethodsWithoutRegistered(String pattern) {
            Set<HttpMethod> registeredMethods = Stream.of(
                            SecurityEndpoints.PUBLIC_ENDPOINTS,
                            SecurityEndpoints.AUTHENTICATED_ENDPOINTS,
                            SecurityEndpoints.ACTIVE_USER_ENDPOINTS,
                            SecurityEndpoints.CERTIFIED_USER_ENDPOINTS
                    )
                    .flatMap(Stream::of)
                    .filter(endpoint -> pattern.equals(endpoint.pattern()))
                    .map(SecurityEndpoint::httpMethod)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return Arrays.stream(HttpMethod.values())
                    .filter(method -> !method.equals(HttpMethod.TRACE) && !registeredMethods.contains(method))
                    .toArray(HttpMethod[]::new);
        }

        private String buildDummyUrl(SecurityEndpoint endpoint) {
            // Wildcard
            String pattern = endpoint.pattern();
            String dummyPath = pattern
                    .replace("**", "test/test")
                    .replace("*", "test");

            // PathVariable
            Pattern pathVarPattern = Pattern.compile("\\{([^}]+)}");
            Matcher matcher = pathVarPattern.matcher(dummyPath);
            StringBuilder resolved = new StringBuilder();
            int counter = 1;

            while (matcher.find()) {
                matcher.appendReplacement(resolved, "test" + counter++);
            }

            matcher.appendTail(resolved);
            dummyPath = resolved.toString();

            return dummyPath;
        }
    }
}
