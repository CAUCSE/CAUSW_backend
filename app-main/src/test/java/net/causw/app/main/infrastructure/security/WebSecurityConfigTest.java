package net.causw.app.main.infrastructure.security;

import static net.causw.app.main.core.security.SecurityEndpoints.SecurityEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import net.causw.app.main.core.security.AppleOAuth2AuthorizationRequestResolver;
import net.causw.app.main.core.security.CustomAuthenticationEntryPoint;
import net.causw.app.main.core.security.CustomAuthorizationManager;
import net.causw.app.main.core.security.JwtTokenProvider;
import net.causw.app.main.core.security.SecurityEndpoints;
import net.causw.app.main.core.security.WebSecurityConfig;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.auth.handler.OAuth2FailureHandler;
import net.causw.app.main.domain.user.auth.handler.OAuth2SuccessHandler;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;
import net.causw.app.main.domain.user.auth.service.v1.SecurityService;
import net.causw.app.main.util.DummyController;
import net.causw.app.main.util.WithMockCustomUser;

@WebMvcTest(DummyController.class)
@Import({CustomAuthorizationManager.class, SecurityService.class, WebSecurityConfig.class,
	CustomAuthenticationEntryPoint.class})
public class WebSecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;
	@MockBean
	private AppleOAuth2AuthorizationRequestResolver appleOAuth2AuthorizationRequestResolver;
	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;
	@MockBean
	private OAuth2SuccessHandler oAuth2SuccessHandler;
	@MockBean
	private OAuth2FailureHandler oAuth2FailureHandler;

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
				: new HttpMethod[] {endpoint.httpMethod()};
		}

		private HttpMethod[] getAllHttpMethodsWithoutRegistered(String pattern) {
			Set<HttpMethod> registeredMethods = Stream.of(
				SecurityEndpoints.PUBLIC_ENDPOINTS,
				SecurityEndpoints.AUTHENTICATED_ENDPOINTS,
				SecurityEndpoints.ACTIVE_USER_ENDPOINTS,
				SecurityEndpoints.CERTIFIED_USER_ENDPOINTS)
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

	@Nested
	@DisplayName("V2 API 권한 테스트")
	class V2ApiSecurityTest {

		@Test
		@WithAnonymousUser
		@DisplayName("/api/v2/auth/** 경로는 인증 없이 접근 허용")
		void shouldAllowV2AuthAccess_WhenAnonymous() throws Exception {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/auth/login"))
				.andExpect(status().isNotFound());
		}

		@Test
		@WithAnonymousUser
		@DisplayName("/api/v2/admin/** 경로는 비로그인 시 접근 거부")
		void shouldRejectV2AdminAccess_WhenAnonymous() throws Exception {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/admin/test"))
				.andExpect(status().isUnauthorized());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("/api/v2/admin/** 경로는 ADMIN 권한 보유 시 접근 허용")
		void shouldAllowV2AdminAccess_WhenAdmin() throws Exception {
			mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/admin/test"))
				.andExpect(status().isNotFound());
		}
	}
}
