package net.causw.config.security;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.causw.domain.model.util.PatternUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Security의 authorizeHttpRequests DSL에서
 * 포함관계(endpoint precedence)를 고려한 권한 설정 정렬을 지원하는 유틸리티 클래스
 * <p>
 * 사용 목적:
 * <ul>
 *     <li>포괄적인 URL 패턴이 더 구체적인 패턴보다 먼저 등록되어 무시되는 것을 방지</li>
 *     <li>접근 제어 로직을 간결하고 선언적으로 작성</li>
 *     <li>AuthorizationManager와 패턴 정보를 그룹핑하여 로그 출력 및 관리 용이</li>
 * </ul>
 * <p>
 * 주요 기능:
 * <ul>
 *     <li>엔드포인트 별 접근 권한을 등록 및 정렬</li>
 *     <li>등록된 권한 정보를 로그로 출력</li>
 * </ul>
 */
@Slf4j
public class RequestAuthorizationBinder {
    private final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry;
    private final List<RequestAuthorization> requestAuthorizations = new ArrayList<>();
    private final PathPatternParser parser = new PathPatternParser();
    private boolean doSort = false;
    private boolean doLog = false;

    private RequestAuthorizationBinder(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        this.registry = registry;
    }

    /**
     * static 팩토리 메서드
     *
     * @param registry Spring Security DSL 내부에서 생성되는 matcher registry
     * @return RequestAuthorizationBinder 인스턴스
     */
    public static RequestAuthorizationBinder with(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        return new RequestAuthorizationBinder(registry);
    }

    /**
     * AuthorizationManager 및 path pattern(문자열 기반)을 바인딩
     *
     * @param name 바인딩 이름 (로그 출력용)
     * @param manager 요청 접근 제어를 위한 AuthorizationManager
     * @param patterns URL 패턴 목록
     * @return this
     */
    public RequestAuthorizationBinder bind(
            String name,
            AuthorizationManager<RequestAuthorizationContext> manager,
            String... patterns
    ) {
        List<RequestAuthorizationBinder.DescriptiveRequestMatcher> matchers = Arrays.stream(patterns)
                .map(pattern -> {
                    String antPath = PatternUtil.toAntPath(pattern);

                    return new RequestAuthorizationBinder.DescriptiveRequestMatcher(
                            new AntPathRequestMatcher(antPath),
                            antPath,
                            null
                    );
                })
                .collect(Collectors.toList());

        return bind(new RequestAuthorization(name, manager, matchers));
    }

    /**
     * AuthorizationManager, HTTP method, path pattern을 함께 바인딩
     *
     * @param name 바인딩 이름 (로그 출력용)
     * @param manager 요청 접근 제어를 위한 AuthorizationManager
     * @param method HTTP 메서드
     * @param patterns URL 패턴 목록
     * @return this
     */
    public RequestAuthorizationBinder bind(
            String name,
            AuthorizationManager<RequestAuthorizationContext> manager,
            HttpMethod method,
            String... patterns
    ) {
        List<RequestAuthorizationBinder.DescriptiveRequestMatcher> matchers = Arrays.stream(patterns)
                .map(pattern -> {
                    String antPath = PatternUtil.toAntPath(pattern);

                    return new RequestAuthorizationBinder.DescriptiveRequestMatcher(
                            new AntPathRequestMatcher(antPath, method.name()),
                            antPath,
                            method
                    );
                })
                .collect(Collectors.toList());

        return bind(new RequestAuthorization(name, manager, matchers));
    }

    /**
     * 미리 정의된 SecurityEndpoint enum 기반으로 AuthorizationManager를 바인딩
     *
     * @param name 바인딩 이름 (로그 출력용)
     * @param manager 요청 접근 제어를 위한 AuthorizationManager
     * @param endpoints 정렬 기준이 되는 보안 엔드포인트 정보
     * @return this
     */
    public RequestAuthorizationBinder bind(
            String name,
            AuthorizationManager<RequestAuthorizationContext> manager,
            SecurityEndpoints.SecurityEndpoint... endpoints
    ) {
        List<RequestAuthorizationBinder.DescriptiveRequestMatcher> matchers = Stream.of(endpoints)
                .map(e -> new RequestAuthorizationBinder.DescriptiveRequestMatcher(
                        e.toRequestMatcher(),
                        PatternUtil.toAntPath(e.pattern()),
                        e.httpMethod()
                ))
                .collect(Collectors.toList());

        return bind(new RequestAuthorization(name, manager, matchers));
    }

    /**
     * RequestAuthorization 객체들을 직접 바인딩
     *
     * @param authorizations 바인딩할 RequestAuthorization 목록
     * @return this
     */
    public RequestAuthorizationBinder bind(RequestAuthorization... authorizations) {
        this.requestAuthorizations.addAll(Arrays.asList(authorizations));
        return this;
    }

    /**
     * 정렬 수행 여부 설정
     * <p>
     * 정렬을 활성화하면 포괄적인 경로보다 더 구체적인 경로가 먼저 등록되어
     * Security 필터 체인에서 덮어쓰기 문제를 방지할 수 있음
     *
     * @param flag true면 apply 시 정렬 수행
     * @return this
     */
    public RequestAuthorizationBinder sort(boolean flag) {
        this.doSort = flag;
        return this;
    }

    /**
     * 로그 출력 여부 설정
     *
     * @param flag true면 apply 시 바인딩된 정보 로그 출력
     * @return this
     */
    public RequestAuthorizationBinder log(boolean flag) {
        this.doLog = flag;
        return this;
    }

    /**
     * 등록된 RequestAuthorization을 Security DSL에 적용
     */
    public void apply() {
        List<RequestAuthorization> redefinedAuthorizations = doSort
                ? sortRequestAuthorization(requestAuthorizations)
                : requestAuthorizations;

        applyRequestAuthorization(redefinedAuthorizations);
        logRequestAuthorization(redefinedAuthorizations);
    }

    private void applyRequestAuthorization(List<RequestAuthorization> authorizations) {
        for (RequestAuthorization authorization : authorizations) {
            for (DescriptiveRequestMatcher drm : authorization.matchers()) {
                registry.requestMatchers(drm.matcher()).access(authorization.authorizationManager());
            }
        }
    }

    private void logRequestAuthorization(List<RequestAuthorization> authorizations) {
        if (!doLog) return;

        StringBuilder sb = new StringBuilder();

        sb.append("Applied request authorizations:\n");

        for (RequestAuthorization authorization : authorizations) {
            sb.append("- ").append(authorization.name()).append("\n");

            for (DescriptiveRequestMatcher drm : authorization.matchers()) {
                sb.append(" └ ").append(drm.pattern()).append(" ")
                        .append(drm.httpMethod == null ? "ALL" : drm.httpMethod).append("\n");
            }
        }

        log.info(sb.toString());
    }

    private List<RequestAuthorization> sortRequestAuthorization(List<RequestAuthorization> authorizations) {
        List<RequestAuthorization> flattened = flattenRequestAuthorization(authorizations);

        flattened.sort(this::compareMatchers);

        return groupRequestAuthorization(flattened);
    }

    private List<RequestAuthorization> flattenRequestAuthorization(List<RequestAuthorization> authorizations) {
        return authorizations.stream()
                .flatMap(auth -> auth.matchers().stream()
                        .map(matcher -> new RequestAuthorization(
                                auth.name,
                                auth.authorizationManager(),
                                Collections.singletonList(matcher)
                        )))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<RequestAuthorization> groupRequestAuthorization(List<RequestAuthorization> authorizations) {
        List<RequestAuthorization> grouped = new ArrayList<>();

        for (RequestAuthorization authorization : authorizations) {
            if (grouped.isEmpty()) {
                grouped.add(authorization);
                continue;
            }

            int lastIndex = grouped.size() - 1;
            RequestAuthorization last = grouped.get(lastIndex);

            if (!last.authorizationManager().equals(authorization.authorizationManager())) {
                grouped.add(authorization);
                continue;
            }

            List<DescriptiveRequestMatcher> mergedMatchers = new ArrayList<>(last.matchers());
            mergedMatchers.addAll(authorization.matchers());

            grouped.set(lastIndex, new RequestAuthorization(
                    last.name(),
                    last.authorizationManager(),
                    mergedMatchers
            ));
        }

        return grouped;
    }

    private int compareMatchers(RequestAuthorization a1, RequestAuthorization a2) {
        String p1 = a1.matchers().get(0).pattern();
        String p2 = a2.matchers().get(0).pattern();

        PathPattern pattern1 = parser.parse(p1);
        PathPattern pattern2 = parser.parse(p2);

        boolean p1IncludesP2 = pattern1.matches(PathContainer.parsePath(p2));
        boolean p2IncludesP1 = pattern2.matches(PathContainer.parsePath(p1));

        if (p1IncludesP2 && !p2IncludesP1) return 1;
        if (p2IncludesP1 && !p1IncludesP2) return -1;

        return p1.compareTo(p2);
    }

    /**
     * 요청 접근 제어 정보를 묶는 불변 데이터 구조
     *
     * @param name 로그 출력용 이름
     * @param authorizationManager 요청 접근 제어 관리자
     * @param matchers 적용될 엔드포인트 및 메서드 정보
     */
    public record RequestAuthorization(
            String name,
            AuthorizationManager<RequestAuthorizationContext> authorizationManager,
            List<DescriptiveRequestMatcher> matchers
    ) {}

    /**
     * 요청 매처의 정보를 부가적으로 담는 불변 데이터 구조
     *
     * @param matcher Spring Security 요청 매처
     * @param pattern 경로 패턴 (정렬 및 로그 출력용)
     * @param httpMethod HTTP 메서드 (null이면 전체)
     */
    private record DescriptiveRequestMatcher(
            RequestMatcher matcher,
            String pattern,
            HttpMethod httpMethod
    ) {}
}
