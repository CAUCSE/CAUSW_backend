package net.causw.app.main.shared.infra.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisUtilsTest {

	private static final String CLEANUP_KEY = "Migration:CollidedRefreshTokenCleanup:v1";

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private SetOperations<String, Object> setOperations;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private RedisUtils redisUtils;

	@Test
	@DisplayName("두 사용자에게 중복 발급된 refresh token만 폐기하고 정상 토큰은 유지한다")
	void purgeCollidedRefreshTokens_purgesOnlyCollidedTokens() {
		//given
		Cursor<String> cursor = cursorOf("UserRefreshTokens:userA", "UserRefreshTokens:userB");

		given(redisTemplate.hasKey(CLEANUP_KEY)).willReturn(false);
		given(redisTemplate.scan(any(ScanOptions.class))).willReturn(cursor);
		given(redisTemplate.opsForSet()).willReturn(setOperations);
		given(setOperations.members("UserRefreshTokens:userA"))
			.willReturn(Set.of("collided-token", "userA-only-token"));
		given(setOperations.members("UserRefreshTokens:userB"))
			.willReturn(Set.of("collided-token"));
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		//when
		int purgedCount = redisUtils.purgeCollidedRefreshTokens();

		//then
		assertEquals(1, purgedCount);
		verify(redisTemplate).delete("RefreshToken:collided-token");
		verify(redisTemplate, never()).delete("RefreshToken:userA-only-token");
		verify(setOperations).remove("UserRefreshTokens:userA", "collided-token");
		verify(setOperations).remove("UserRefreshTokens:userB", "collided-token");
		verify(valueOperations).set(CLEANUP_KEY, "DONE");
	}

	@Test
	@DisplayName("이미 정리가 수행된 경우 다시 스캔하지 않는다")
	void purgeCollidedRefreshTokens_skipsWhenAlreadyDone() {
		given(redisTemplate.hasKey(CLEANUP_KEY)).willReturn(true);

		int purgedCount = redisUtils.purgeCollidedRefreshTokens();

		assertEquals(0, purgedCount);
		verify(redisTemplate, never()).scan(any(ScanOptions.class));
	}

	@SuppressWarnings("unchecked")
	private Cursor<String> cursorOf(String... keys) {
		Cursor<String> cursor = mock(Cursor.class);
		Iterator<String> iterator = List.of(keys).iterator();
		given(cursor.hasNext()).willAnswer(invocation -> iterator.hasNext());
		given(cursor.next()).willAnswer(invocation -> iterator.next());
		return cursor;
	}
}
