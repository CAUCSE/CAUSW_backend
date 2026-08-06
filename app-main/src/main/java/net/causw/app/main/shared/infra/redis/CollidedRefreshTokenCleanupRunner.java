package net.causw.app.main.shared.infra.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "redis.migration.collided-refresh-token-cleanup", name = "enabled", havingValue = "true")
public class CollidedRefreshTokenCleanupRunner {

	private final RedisUtils redisUtils;

	@EventListener(ApplicationReadyEvent.class)
	public void purgeCollidedRefreshTokens() {
		try {
			int purgedCount = redisUtils.purgeCollidedRefreshTokens();
			log.info("[Redis Migration] 중복 발급된 refresh token 정리 완료. purgedCount={}", purgedCount);
		} catch (Exception e) {
			log.error("[Redis Migration] 중복 발급된 refresh token 정리 실패. 재시도가 필요합니다.", e);
		}
	}
}
