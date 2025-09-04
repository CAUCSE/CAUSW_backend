package net.causw.app.main.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.causw.app.main.infrastructure.security.SecurityEndpoints.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SecurityEndpointsTest {

	private static final Map<String, SecurityEndpoint[]> endpointGroups = Map.of(
		"PUBLIC", PUBLIC_ENDPOINTS,
		"AUTHENTICATED", AUTHENTICATED_ENDPOINTS,
		"ACTIVE_USER", ACTIVE_USER_ENDPOINTS,
		"CERTIFIED_USER", CERTIFIED_USER_ENDPOINTS
	);

	@ParameterizedTest
	@MethodSource("getEndpointGroups")
	@DisplayName("같은 그룹 내 엔드포인트에 중복이 존재할 경우 실패(null method 고려)")
	void shouldFail_whenDuplicateExistsWithinGroup(Map.Entry<String, SecurityEndpoint[]> endpointGroup) {
		String groupName = endpointGroup.getKey();
		SecurityEndpoint[] endpoints = endpointGroup.getValue();

		Set<EndpointKey> seenEndpoints = new HashSet<>();
		Set<String> nullMethodPatterns = new HashSet<>();
		Set<String> allPatterns = new HashSet<>();

		for (SecurityEndpoint endpoint : endpoints) {
			String pattern = endpoint.pattern();
			HttpMethod method = endpoint.httpMethod();
			EndpointKey key = new EndpointKey(pattern, method);

			if (method == null) {
				assertThat(allPatterns.contains(pattern))
					.withFailMessage("그룹 [%s] 내 pattern [%s]이 이미 특정 method로 등록되었는데 null로 중복",
						groupName, pattern)
					.isFalse();
				nullMethodPatterns.add(pattern);
			} else {
				assertThat(nullMethodPatterns.contains(pattern))
					.withFailMessage("그룹 [%s] 내 pattern [%s]이 이미 null로 등록되었는데 %s method로 중복",
						groupName, pattern, method.name())
					.isFalse();
			}

			assertThat(seenEndpoints.add(key))
				.withFailMessage("그룹 [%s] 내 중복 엔드포인트 발견: (%s %s)",
					groupName, method, pattern)
				.isTrue();

			allPatterns.add(pattern);
		}
	}

	@Test
	@DisplayName("서로 다른 그룹 간에 동일한 (pattern, method)가 존재할 경우 실패")
	void shouldFail_whenDuplicateExistsBetweenGroups() {
		Map<EndpointKey, String> globalMap = new HashMap<>();

		for (Map.Entry<String, SecurityEndpoint[]> entry : endpointGroups.entrySet()) {
			String groupName = entry.getKey();
			SecurityEndpoint[] endpoints = entry.getValue();

			for (SecurityEndpoint endpoint : endpoints) {
				EndpointKey key = new EndpointKey(endpoint.pattern(), endpoint.httpMethod());

				assertThat(globalMap.putIfAbsent(key, groupName))
					.withFailMessage("서로 다른 그룹 [%s, %s] 간 중복 엔드포인트 발견: (%s %s)",
						globalMap.get(key), groupName, key.method(), key.pattern())
					.isNull();
			}
		}
	}

	private static Set<Map.Entry<String, SecurityEndpoint[]>> getEndpointGroups() {
		return endpointGroups.entrySet();
	}

	private record EndpointKey(String pattern, HttpMethod method) {
	}
}
