package net.causw.config.security;

public class SecurityEndpoints {
    public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/css/**",
            "/images/**",
            "/js/**",
            "/favicon.ico",
            "/h2-console/**",
            "/api/v1/users/sign-in",
            "/api/v1/users/sign-up",
            "/healthy",
            "/api/v1/users/admissions/apply",
            "/api/v1/users/{email}/is-duplicated",
            "/api/v1/users/{nickname}/is-duplicated-nickname",
            "/api/v1/users/{studentId}/is-duplicated-student-id",
            "/api/v1/users/email",
            "/api/v1/users/password",
            "/api/v1/users/token/update",
            "/api/v1/storage/**",
            "/api/v1/users/password/find",
            "/api/v1/users/user-id/find",
            "/swagger-ui/**",
            "/api/v1/fcm/send",
            "/v3/api-docs/**",
            "/actuator/**"
    };

    public static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/v1/posts/app/notice",
            "/api/v1/users/me",
            "/api/v1/users/admissions/self",
            "/api/v1/users/sign-out",
            "/api/v1/users/studentId/{studentId}"
    };

    public static final String[] ACTIVE_USER_ENDPOINTS = {
            "/api/v1/users/academic-record/**"
    };

    public static final String[] CERTIFIED_USER_ENDPOINTS = {
            "/api/v1/users/academic-record/export",
            "/api/v1/boards/**",
            "/api/v1/calendars/**",
            "/api/v1/ceremony/**",
            "/api/v1/child-comments/**",
            "/api/v1/circles/**",
            "/api/v1/comments/**",
            "/api/v1/events/**",
            "/api/v1/forms/**",
            "/api/v1/lockers/**",
            "/api/v1/notifications/log/**",
            "/api/v1/posts/**"
    };
}
