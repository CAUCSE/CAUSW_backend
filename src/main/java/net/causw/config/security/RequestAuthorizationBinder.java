package net.causw.config.security;

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

    public RequestAuthorizationBinder bind(AuthorizationManager<RequestAuthorizationContext> manager, String... patterns) {
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

        return bind(new RequestAuthorization(manager, matchers));
    }

    public RequestAuthorizationBinder bind(AuthorizationManager<RequestAuthorizationContext> manager, HttpMethod method, String... patterns) {
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

        return bind(new RequestAuthorization(manager, matchers));
    }

    public RequestAuthorizationBinder bind(
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

        return bind(new RequestAuthorization(manager, matchers));
    }

    public RequestAuthorizationBinder bind(RequestAuthorization... rules) {
        this.requestAuthorizations.addAll(Arrays.asList(rules));
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
        List<RequestAuthorization> refinedRules = doSort ? splitAndSortRules(requestAuthorizations) : requestAuthorizations;

        applyRequestAuthorization(refinedRules);
        logRequestAuthorization(refinedRules);
    }

    private void applyRequestAuthorization(List<RequestAuthorization> rules) {
        for (RequestAuthorization rule : rules) {
            for (DescriptiveRequestMatcher drm : rule.matchers()) {
                registry.requestMatchers(drm.matcher()).access(rule.authorizationManager());
            }
        }
    }

    private void logRequestAuthorization(List<RequestAuthorization> rules) {
        if (!doLog) return;

        for (RequestAuthorization rule : rules) {
            log.info("- {}", rule.authorizationManager());
            for (DescriptiveRequestMatcher drm : rule.matchers()) {
                log.info(" └ {}", drm.pattern());
            }
        }
    }

    private List<RequestAuthorization> splitAndSortRules(List<RequestAuthorization> rules) {
        List<DescriptiveRequestMatcher> allMatchers = rules.stream()
                .flatMap(rule -> rule.matchers().stream()
                        .map(matcher -> new DescriptiveRequestMatcher(matcher.matcher(), matcher.pattern(), matcher.httpMethod())))
                .sorted(this::compareMatchers)
                .toList();

        Map<AuthorizationManager<RequestAuthorizationContext>, List<DescriptiveRequestMatcher>> grouped = new LinkedHashMap<>();
        for (RequestAuthorization rule : rules) {
            grouped.computeIfAbsent(rule.authorizationManager(), k -> new ArrayList<>());
        }

        for (DescriptiveRequestMatcher drm : allMatchers) {
            for (RequestAuthorization rule : rules) {
                if (rule.matchers().contains(drm)) {
                    grouped.get(rule.authorizationManager()).add(drm);
                    break;
                }
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> new RequestAuthorization(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private int compareMatchers(DescriptiveRequestMatcher m1, DescriptiveRequestMatcher m2) {
        String p1 = m1.pattern();
        String p2 = m2.pattern();

        PathPattern pattern1 = parser.parse(p1);
        PathPattern pattern2 = parser.parse(p2);

        boolean p1IncludesP2 = pattern1.matches(PathContainer.parsePath(p2));
        boolean p2IncludesP1 = pattern2.matches(PathContainer.parsePath(p1));

        if (p1IncludesP2 && !p2IncludesP1) return 1;
        if (p2IncludesP1 && !p1IncludesP2) return -1;

        return p1.compareTo(p2);
    }

    public record RequestAuthorization(
            AuthorizationManager<RequestAuthorizationContext> authorizationManager,
            List<DescriptiveRequestMatcher> matchers
    ) {}

    private record DescriptiveRequestMatcher(
            RequestMatcher matcher,
            String pattern,
            HttpMethod httpMethod
    ) {}
}
