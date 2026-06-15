package net.causw.app.main.infrastructure.security;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import net.causw.app.main.core.security.AppleOAuth2AuthorizationRequestResolver;
import net.causw.app.main.core.security.CustomAuthenticationEntryPoint;
import net.causw.app.main.core.security.JwtTokenProvider;
import net.causw.app.main.core.security.OAuth2AuthorizationRequestCookieRepository;
import net.causw.app.main.core.security.WebSecurityConfig;
import net.causw.app.main.domain.user.auth.handler.OAuth2FailureHandler;
import net.causw.app.main.domain.user.auth.handler.OAuth2SuccessHandler;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;
import net.causw.app.main.domain.user.auth.service.implementation.OAuth2RefreshTokenCaptureClient;
import net.causw.app.main.util.DummyController;

@WebMvcTest(DummyController.class)
@Import({WebSecurityConfig.class, CustomAuthenticationEntryPoint.class})
public class WebSecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;
	@MockBean
	private AppleOAuth2AuthorizationRequestResolver appleOAuth2AuthorizationRequestResolver;
	@MockBean
	private OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;
	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;
	@MockBean
	private OAuth2SuccessHandler oAuth2SuccessHandler;
	@MockBean
	private OAuth2FailureHandler oAuth2FailureHandler;
	@MockBean
	private OAuth2RefreshTokenCaptureClient oAuth2RefreshTokenCaptureClient;
	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@Test
	@WithAnonymousUser
	@DisplayName("/api/v2/auth/** 경로는 인증 없이 접근 허용")
	void givenAnonymousUser_whenRequestV2Auth_thenSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/auth/login"))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithAnonymousUser
	@DisplayName("/api/v2/admin/** 경로는 비로그인 시 접근 거부")
	void givenAnonymousUser_whenRequestV2Admin_thenUnauthorized() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/admin/test"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("/api/v2/admin/** 경로는 ADMIN 권한 보유 시 접근 허용")
	void givenAdminUser_whenRequestV2Admin_thenSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/admin/test"))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithAnonymousUser
	@DisplayName("/api/v2/terms 경로는 인증 없이 접근 허용")
	void givenAnonymousUser_whenRequestV2Terms_thenSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/terms"))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithAnonymousUser
	@DisplayName("/api/v2/users/password-change 경로는 인증 없이 접근 허용")
	void givenAnonymousUser_whenRequestV2PasswordChange_thenSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/users/password-change"))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithAnonymousUser
	@DisplayName("/api/v1/** 경로는 v1 보안 체인에 걸리지 않는다")
	void givenAnonymousUser_whenRequestV1Path_thenNotSecuredByV1Chain() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/me"))
			.andExpect(status().isNotFound());
	}
}
