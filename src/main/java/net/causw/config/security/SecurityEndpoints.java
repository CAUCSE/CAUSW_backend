package net.causw.config.security;

import net.causw.domain.model.util.PatternUtil;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static net.causw.config.security.SecurityEndpoints.SecurityEndpoint.*;
import static org.springframework.http.HttpMethod.*;

public class SecurityEndpoints {
    public static final SecurityEndpoint[] PUBLIC_ENDPOINTS = {
            of("/"),
            of("/css/**"),
            of("/images/**"),
            of("/js/**"),
            of("/favicon.ico", GET),
            of("/h2-console/**"),
            of("/api/v1/users/sign-in", POST),
            of("/api/v1/users/sign-up", POST),
            of("/healthy", GET),
            of("/api/v1/users/admissions/apply", POST),
            of("/api/v1/users/{email}/is-duplicated", GET),
            of("/api/v1/users/{nickname}/is-duplicated-nickname", GET),
            of("/api/v1/users/{studentId}/is-duplicated-student-id", GET),
            of("/api/v1/users/password", PUT),
            of("/api/v1/users/token/update", PUT),
            of("/api/v1/storage/**"),
            of("/api/v1/users/password/find", PUT),
            of("/api/v1/users/user-id/find", POST),
            of("/swagger-ui/**"),
            of("/api/v1/fcm/send", POST),
            of("/v3/api-docs/**"),
            of("/actuator/**")
    };

    public static final SecurityEndpoint[] AUTHENTICATED_ENDPOINTS = {
            of("/api/v1/posts/app/notice", GET),
            of("/api/v1/users/me", GET),
            of("/api/v1/users/admissions/self", GET),
            of("/api/v1/users/sign-out", POST),
            of("/api/v1/users/studentId/{studentId}", GET)
    };

    public static final SecurityEndpoint[] ACTIVE_USER_ENDPOINTS = {
            of("/api/v1/users/academic-record/**")
    };

    public static final SecurityEndpoint[] CERTIFIED_USER_ENDPOINTS = {
            of("/api/v1/home", GET),
            of("/api/v1/users/academic-record/export", GET),
            of("/api/v1/votes/{voteId}", GET),
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
            of("/api/v1/posts/**"),
            of("/api/v1/semesters/**")
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
