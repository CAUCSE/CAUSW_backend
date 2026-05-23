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
@ConditionalOnProperty(prefix = "redis.migration.refresh-token-user-index", name = "enabled", havingValue = "true")
public class RefreshTokenUserIndexMigrationRunner {

	private final RedisUtils redisUtils;

	@EventListener(ApplicationReadyEvent.class)
	public void migrateRefreshTokenUserIndex() {
		int migratedCount = redisUtils.migrateRefreshTokenUserIndex();
		if (migratedCount > 0) {
			log.info("[Redis Migration] 사용자별 refresh token 인덱스 백필 완료. migratedCount={}", migratedCount);
		}
	}
}
