package net.causw.config.security;

import lombok.RequiredArgsConstructor;
import net.causw.application.security.SecurityService;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

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
