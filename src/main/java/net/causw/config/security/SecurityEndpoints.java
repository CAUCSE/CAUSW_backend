package net.causw.config.security;

import net.causw.domain.model.util.PatternUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static net.causw.config.security.SecurityEndpoints.SecurityEndpoint.*;

public class SecurityEndpoints {
    public static final SecurityEndpoint[] PUBLIC_ENDPOINTS = {
            of("/"),
            of("/css/**"),
            of("/images/**"),
            of("/js/**"),
            of("/favicon.ico"),
            of("/h2-console/**"),
            of("/api/v1/users/sign-in"),
            of("/api/v1/users/sign-up"),
            of("/healthy"),
            of("/api/v1/users/admissions/apply"),
            of("/api/v1/users/{email}/is-duplicated"),
            of("/api/v1/users/{nickname}/is-duplicated-nickname"),
            of("/api/v1/users/{studentId}/is-duplicated-student-id"),
            of("/api/v1/users/email"),
            of("/api/v1/users/password"),
            of("/api/v1/users/token/update"),
            of("/api/v1/storage/**"),
            of("/api/v1/users/password/find"),
            of("/api/v1/users/user-id/find"),
            of("/swagger-ui/**"),
            of("/api/v1/fcm/send"),
            of("/v3/api-docs/**"),
            of("/actuator/**")
    };

    public static final SecurityEndpoint[] AUTHENTICATED_ENDPOINTS = {
            of("/api/v1/posts/app/notice"),
            of("/api/v1/users/me"),
            of("/api/v1/users/admissions/self"),
            of("/api/v1/users/sign-out"),
            of("/api/v1/users/studentId/{studentId}")
    };

    public static final SecurityEndpoint[] ACTIVE_USER_ENDPOINTS = {
            of("/api/v1/users/academic-record/**")
    };

    public static final SecurityEndpoint[] CERTIFIED_USER_ENDPOINTS = {
            of("/api/v1/users/academic-record/export"),
            of("/api/v1/boards/**"),
            of("/api/v1/calendars/**"),
            of("/api/v1/ceremony/**"),
            of("/api/v1/child-comments/**"),
            of("/api/v1/circles/**"),
            of("/api/v1/comments/**"),
            of("/api/v1/events/**"),
            of("/api/v1/forms/**"),
            of("/api/v1/lockers/**"),
            of("/api/v1/notifications/log/**"),
            of("/api/v1/posts/**")
    };

    public record SecurityEndpoint(
            String pattern,
            HttpMethod httpMethod
    ) {
        public static SecurityEndpoint of(String pattern) {
            return new SecurityEndpoint(pattern, null);
        }

        public static SecurityEndpoint of(String pattern, HttpMethod method) {
            return new SecurityEndpoint(pattern, method);
        }

        public RequestMatcher toRequestMatcher() {
            return httpMethod != null
                    ? new AntPathRequestMatcher(PatternUtil.toAntPath(pattern), httpMethod.name())
                    : new AntPathRequestMatcher(PatternUtil.toAntPath(pattern));
        }
    }
}
