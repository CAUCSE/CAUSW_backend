package net.causw.app.main.core.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import net.causw.app.main.domain.user.auth.handler.OAuth2FailureHandler;
import net.causw.app.main.domain.user.auth.handler.OAuth2SuccessHandler;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;
import net.causw.app.main.domain.user.auth.service.implementation.OAuth2RefreshTokenCaptureClient;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAuthorizationManager authorizationManager;
	private final AppleOAuth2AuthorizationRequestResolver appleOAuth2AuthorizationRequestResolver;
	private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final OAuth2RefreshTokenCaptureClient oAuth2RefreshTokenCaptureClient;

	@Value("${app.cors.allowed-origins:http://localhost:3000}")
	private String corsAllowedOrigins;

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Order(1)
	public SecurityFilterChain securityFilterChainV2(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/api/v2/**", "/oauth2/**", "/login/oauth2/**")
			.cors(cors -> cors.configurationSource(corsConfigurationSourceV2()))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers("/api/v2/auth/logout", "/api/v2/auth/onboarding/**").authenticated()
				.requestMatchers("/api/v2/auth/**", "/oauth2/**", "/login/oauth2/**",
					"/api/v2/users/check-nickname", "/api/v2/users/check-phone",
					"/api/v2/users/password-change")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v2/terms")
				.permitAll()
				.requestMatchers("/api/v2/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
					.authorizationRequestResolver(appleOAuth2AuthorizationRequestResolver)
					.authorizationRequestRepository(oAuth2AuthorizationRequestCookieRepository))
				.tokenEndpoint(tokenEndpoint -> tokenEndpoint
					.accessTokenResponseClient(oAuth2RefreshTokenCaptureClient))
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
					.oidcUserService(customOAuth2UserService::loadOidcUser))
				.successHandler(oAuth2SuccessHandler)
				.failureHandler(oAuth2FailureHandler))
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(customAuthenticationEntryPoint))
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customAuthenticationEntryPoint),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain securityFilterChainV1(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSourceV1()))
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(registry -> {
				registry.requestMatchers(CorsUtils::isPreFlightRequest).permitAll();
				RequestAuthorizationBinder.with(registry)
					.bind("Public", authorizationManager.permitAll(), SecurityEndpoints.PUBLIC_ENDPOINTS)
					.bind("Authenticated", authorizationManager.authenticated(),
						SecurityEndpoints.AUTHENTICATED_ENDPOINTS)
					.bind("Active", authorizationManager.isActiveUser(), SecurityEndpoints.ACTIVE_USER_ENDPOINTS)
					.bind("Certified", authorizationManager.isCertifiedUser(),
						SecurityEndpoints.CERTIFIED_USER_ENDPOINTS)
					.sort(true)
					.log(true)
					.apply();
				registry.anyRequest().authenticated();
			})
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(customAuthenticationEntryPoint))
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, customAuthenticationEntryPoint),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSourceV2() {
		CorsConfiguration configuration = new CorsConfiguration();
		List<String> origins = Arrays.asList(corsAllowedOrigins.split(","));
		configuration.setAllowedOriginPatterns(origins);
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSourceV1() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/webjars/**");
	}

	@Bean
	public JwtDecoderFactory<ClientRegistration> oidcIdTokenDecoderFactory() {
		OidcIdTokenDecoderFactory factory = new OidcIdTokenDecoderFactory();

		// Spring 기본 OIDC 검증기(OidcIdTokenValidator)가 azp 등을 추가로 강제할 수 있어,
		// native login에서 사용하는 id_token 검증에는 timestamp/issuer/audience만 검증하도록 커스텀한다.
		factory.setJwtValidatorFactory(clientRegistration -> {
			OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();

			String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();
			OAuth2TokenValidator<Jwt> issuerValidator = org.springframework.util.StringUtils.hasText(issuerUri)
				? new JwtIssuerValidator(issuerUri)
				: token -> OAuth2TokenValidatorResult.success();

			String clientId = clientRegistration.getClientId();
			OAuth2TokenValidator<Jwt> audienceValidator = token -> {
				if (token.getAudience() != null && token.getAudience().contains(clientId)) {
					return OAuth2TokenValidatorResult.success();
				}
				return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
			};

			return new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator, audienceValidator);
		});

		return factory;
	}
}
