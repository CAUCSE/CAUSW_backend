package net.causw.app.main.infrastructure.security;

import net.causw.global.util.PatternUtil;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static net.causw.app.main.infrastructure.security.SecurityEndpoints.SecurityEndpoint.*;
import static org.springframework.http.HttpMethod.*;

/**
 * Spring Security에서 사용할 엔드포인트 패턴을 그룹별로 정의한 클래스
 * <p>
 * SecurityService의 래퍼인 {@link CustomAuthorizationManager}와 함께
 * {@link RequestAuthorizationBinder}에서 사용되어,
 * 인증/인가 정책을 선언적으로 정의할 수 있도록 함
 * <p>
 * 대다수의 도메인에서 특정 서비스에 대한 @PreAuthorize 사용이 잦을 경우 SecurityEndpoint로 리팩토링하고,
 * SecurityEndpoints에서 사용된 서비스의 경우 @PreAuthorize를 통한 사용을 지양할 것을 권장함
 */
public class SecurityEndpoints {

	/**
	 * 인증 없이 누구나 접근 가능한 엔드포인트 목록
	 * <p>permitAll 정책이 적용됨
	 */
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
			of("/actuator/**"),
			of("/ws-connect/**")
	};

	/**
	 * 인증된 사용자만 접근 가능한 엔드포인트 목록
	 * <p>authenticated 정책이 적용됨
	 */
	public static final SecurityEndpoint[] AUTHENTICATED_ENDPOINTS = {
			of("/api/v1/posts/app/notice", GET),
			of("/api/v1/users/me", GET),
			of("/api/v1/users/admissions/self", GET),
			of("/api/v1/users/sign-out", POST),
			of("/api/v1/users/studentId/{studentId}", GET)
	};

	/**
	 * 활성 사용자 (UserState.ACTIVE 및 NONE 외 역할 보유자)만 접근 가능한 엔드포인트 목록
	 * <p>isActiveUser 정책이 적용됨
	 */
	public static final SecurityEndpoint[] ACTIVE_USER_ENDPOINTS = {
			of("/api/v1/users/academic-record/**")
	};

	/**
	 * 학적 인증된 사용자(AcademicStatus.UNDETERMINED가 아니고
	 * UserState.ACTIVE 및 NONE 외 역할 보유자)만 접근 가능한 엔드포인트 목록
	 * <p>isCertifiedUser 정책이 적용됨
	 */
	public static final SecurityEndpoint[] CERTIFIED_USER_ENDPOINTS = {
			of("/api/v1/home", GET),
			of("/api/v1/home/alumni", GET),
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
			of("/api/v1/reports/**"),
			of("/api/v1/semesters/**")
	};

	/**
	 * 엔드포인트 정보(패턴 + HTTP 메서드)를 표현하는 불변 객체
	 * <p>
	 * {@link RequestAuthorizationBinder}에서 request matcher로 변환하는 데 사용됨
	 *
	 * @param pattern URL 패턴
	 * @param httpMethod null 가능, 특정 메서드가 지정되지 않은 경우 전체 허용
	 */
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
