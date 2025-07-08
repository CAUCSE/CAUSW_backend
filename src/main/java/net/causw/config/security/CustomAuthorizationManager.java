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

    public AuthorizationManager<RequestAuthorizationContext> isActiveAndNotNoneUserAndAcademicRecordCertified() {
        return (authentication, context) -> {
            boolean isAuthenticated = securityService.isActiveAndNotNoneUserAndAcademicRecordCertified();
            return new AuthorizationDecision(isAuthenticated);
        };
    }

    public AuthorizationManager<RequestAuthorizationContext> isActiveAndNotNoneUser() {
        return (authentication, context) -> {
            boolean isAuthenticated = securityService.isActiveAndNotNoneUser();
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
