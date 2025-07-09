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

    public static RequestAuthorizationBinder with(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        return new RequestAuthorizationBinder(registry);
    }

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

    public RequestAuthorizationBinder bind(RequestAuthorization... authorizations) {
        this.requestAuthorizations.addAll(Arrays.asList(authorizations));
        return this;
    }

    public RequestAuthorizationBinder sort(boolean flag) {
        this.doSort = flag;
        return this;
    }

    public RequestAuthorizationBinder log(boolean flag) {
        this.doLog = flag;
        return this;
    }

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
                sb.append(" └ ").append(drm.pattern()).append("\n");
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

    public record RequestAuthorization(
            String name,
            AuthorizationManager<RequestAuthorizationContext> authorizationManager,
            List<DescriptiveRequestMatcher> matchers
    ) {}

    private record DescriptiveRequestMatcher(
            RequestMatcher matcher,
            String pattern,
            HttpMethod httpMethod
    ) {}
}
