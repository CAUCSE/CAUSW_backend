package net.causw.app.main.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!prod")
@Configuration
@EnableWebSecurity
public class SwaggerSecurityConfig {

	@Value("${swagger.username}")
	private String swaggerUsername;

	@Value("${swagger.password}")
	private String swaggerPassword;

	@Bean
	@Order(0)
	public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/login")
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
	 * @Bean 어노테이션을 빼서 스프링 글로벌 컨텍스트에 등록하지 않습니다.
	 * 이렇게 하면 기존 REST API의 DB 연동 UserDetailsService 등과 절대 충돌하지 않습니다.
	 */
	private AuthenticationProvider swaggerAuthenticationProvider() {
		UserDetails user = User.withUsername(swaggerUsername)
			.password("{noop}" + swaggerPassword)
			.roles("ADMIN")
			.build();

		InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager(user);

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);

		return provider;
	}
}
