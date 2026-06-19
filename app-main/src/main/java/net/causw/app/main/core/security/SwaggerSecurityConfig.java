package net.causw.app.main.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

public class SwaggerSecurityConfig {

	private static final String[] SWAGGER_PATHS = {
		"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
	};

	private static final String[] SWAGGER_LOGIN_PATHS = {
		"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/login"
	};

	/** prod: Swagger 경로 전면 차단 (403) */
	@Profile("prod")
	@Configuration
	public static class ProdConfig {

		@Bean
		@Order(0)
		public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
			http
				.securityMatcher(SWAGGER_PATHS)
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
			return http.build();
		}
	}

	/** local: Swagger 경로 인증 없이 전면 허용 */
	@Profile("local")
	@Configuration
	public static class LocalConfig {

		@Bean
		@Order(0)
		public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
			http
				.securityMatcher(SWAGGER_PATHS)
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
			return http.build();
		}
	}

	/** prod·local 외(예: dev): 폼 로그인으로 Swagger 접근 제어 */
	@Profile("!prod & !local")
	@Configuration
	public static class DefaultConfig {

		@Value("${swagger.username:admin}")
		private String swaggerUsername;

		@Value("${swagger.password:admin}")
		private String swaggerPassword;

		@Bean
		@Order(0)
		public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
			http
				.securityMatcher(SWAGGER_LOGIN_PATHS)
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
					.requestMatchers("/login").permitAll()
					.anyRequest().authenticated())
				.formLogin(form -> form
					.defaultSuccessUrl("/swagger-ui/index.html", true))
				.authenticationProvider(swaggerAuthenticationProvider());
			return http.build();
		}

		/**
		 * @Bean 없이 직접 생성 — 글로벌 컨텍스트에 등록되지 않으므로
		 * REST API의 DB 연동 UserDetailsService와 충돌하지 않는다.
		 */
		private AuthenticationProvider swaggerAuthenticationProvider() {
			UserDetails user = User.withUsername(swaggerUsername)
				.password("{noop}" + swaggerPassword)
				.roles("ADMIN")
				.build();

			InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager(user);

			DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

			return provider;
		}
	}
}
