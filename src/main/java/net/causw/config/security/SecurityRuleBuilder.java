package net.causw.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.*;

@Slf4j
public class SecurityRuleBuilder {
    private final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry;
    private final List<SecurityRule> rules = new ArrayList<>();
    private final PathPatternParser parser = new PathPatternParser();
    private boolean doSort = false;
    private boolean doLog = false;

    public SecurityRuleBuilder(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        this.registry = registry;
    }

    public SecurityRuleBuilder addRule(AuthorizationManager<RequestAuthorizationContext> authorizationManager, String... endpoints) {
        rules.add(new SecurityRule(authorizationManager, Arrays.asList(endpoints)));
        return this;
    }

    public SecurityRuleBuilder sort(boolean flag) {
        this.doSort = flag;
        return this;
    }

    public SecurityRuleBuilder log(boolean flag) {
        this.doLog = flag;
        return this;
    }

    public void apply() {
        List<SecurityRule> refinedRules = doSort ? splitAndSortRules(rules) : rules;

        applyRules(refinedRules);
        logRules(refinedRules);
    }

    private void applyRules(List<SecurityRule> rules) {
        for (SecurityRule rule : rules) {
            registry.requestMatchers(rule.endpoints().getFirst()).access(rule.authorizationManager());
        }
    }

    private void logRules(List<SecurityRule> rules) {
        if (!doLog)
            return;

        for (SecurityRule rule : rules) {
            log.info("- {}", rule.authorizationManager());
            for (String endpoint : rule.endpoints()) {
                log.info(" └ {}", endpoint);
            }
        }
    }

    private List<SecurityRule> splitAndSortRules(List<SecurityRule> rules) {
        List<SecurityRule> result = new ArrayList<>(flattenRules(rules));

        result.sort(this::compareByInclusion);

        return result;
    }

    private List<SecurityRule> flattenRules(List<SecurityRule> rules) {
        return rules.stream()
                .flatMap(rule -> rule.endpoints().stream()
                        .map(endpoint -> new SecurityRule(rule.authorizationManager(), Collections.singletonList(endpoint))))
                .toList();
    }

    private int compareByInclusion(SecurityRule r1, SecurityRule r2){
        String p1 = r1.endpoints().get(0);
        String p2 = r2.endpoints().get(0);

        PathPattern pattern1 = parser.parse(p1);
        PathPattern pattern2 = parser.parse(p2);

        boolean p1IncludesP2 = pattern1.matches(PathContainer.parsePath(p2));
        boolean p2IncludesP1 = pattern2.matches(PathContainer.parsePath(p1));

        if (p1IncludesP2 && !p2IncludesP1) return 1;
        if (p2IncludesP1 && !p1IncludesP2) return -1;

        return p1.compareTo(p2);
    }
}

record SecurityRule(
        AuthorizationManager<RequestAuthorizationContext> authorizationManager,
        List<String> endpoints
) {
    SecurityRule(AuthorizationManager<RequestAuthorizationContext> authorizationManager, List<String> endpoints) {
        this.authorizationManager = authorizationManager;
        this.endpoints = new ArrayList<>(endpoints);
    }
}
