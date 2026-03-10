package net.causw.app.main.core.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import net.causw.app.main.domain.user.auth.handler.OAuth2FailureHandler;
import net.causw.app.main.domain.user.auth.handler.OAuth2SuccessHandler;
import net.causw.app.main.domain.user.auth.service.CustomOAuth2UserService;

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
				.requestMatchers("/api/v2/auth/logout").authenticated()
				.requestMatchers("/api/v2/auth/**", "/oauth2/**", "/login/oauth2/**", "/api/v2/users/password-reset/**",
                         "/api/v2/users/check-nickname", "/api/v2/users/check-phone")
				.permitAll()
				.requestMatchers("/api/v2/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
					.authorizationRequestResolver(appleOAuth2AuthorizationRequestResolver)
					.authorizationRequestRepository(oAuth2AuthorizationRequestCookieRepository))
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
					.oidcUserService(customOAuth2UserService::loadOidcUser))
				.successHandler(oAuth2SuccessHandler)
				.failureHandler(oAuth2FailureHandler))
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(customAuthenticationEntryPoint))
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

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
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

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
}
