package net.causw.app.main.infrastructure.security;

import lombok.RequiredArgsConstructor;

import net.causw.app.main.infrastructure.security.SecurityService;

import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/**
 * Spring Security의 AuthorizationManager를 선언적으로 정의하는 래퍼 클래스
 * <p>
 * SecurityService의 인증/인가 로직을 래핑하여 Security DSL (authorizeHttpRequests)에서
 * 재사용 가능하도록 추상화된 AuthorizationManager로 제공
 * <p>
 * 이 클래스 자체는 비즈니스 로직을 갖지 않고,
 * 인증 상태 판별은 내부적으로 {@link SecurityService}에 위임함
 */
@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager {
	private final SecurityService securityService;

	public AuthorizationManager<RequestAuthorizationContext> isCertifiedUser() {
		return (authentication, context) -> {
			boolean isAuthenticated = securityService.isCertifiedUser();
			return new AuthorizationDecision(isAuthenticated);
		};
	}

	public AuthorizationManager<RequestAuthorizationContext> isActiveUser() {
		return (authentication, context) -> {
			boolean isAuthenticated = securityService.isActiveUser();
			return new AuthorizationDecision(isAuthenticated);
		};
	}

	public AuthorizationManager<RequestAuthorizationContext> permitAll() {
		return (authentication, context) -> new AuthorizationDecision(true);
	}

	public AuthorizationManager<RequestAuthorizationContext> authenticated() {
		return AuthenticatedAuthorizationManager.authenticated();
	}
}
