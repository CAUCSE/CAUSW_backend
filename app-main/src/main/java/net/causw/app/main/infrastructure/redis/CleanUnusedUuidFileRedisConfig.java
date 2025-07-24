package net.causw.app.main.infrastructure.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CleanUnusedUuidFileRedisConfig {

	@Autowired
	private RedisTemplateBuilder templateBuilder;

	// UUID 파일 정리 배치 작업 전용 RedisTemplate
	@Bean("cleanUnusedUuidFileRedisTemplate")
	public RedisTemplate<String, String> cleanUnusedUuidFileRedisTemplate(RedisConnectionFactory connectionFactory) {
		return templateBuilder.createStringTemplate(connectionFactory);
	}
} 