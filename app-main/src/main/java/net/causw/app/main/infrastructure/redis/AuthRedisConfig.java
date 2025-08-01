package net.causw.app.main.infrastructure.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AuthRedisConfig {

	@Autowired
	private RedisTemplateBuilder templateBuilder;

	// JWT 토큰 관리를 위한 String 기반 템플릿
	@Bean("authRedisTemplate")
	public RedisTemplate<String, String> authRedisTemplate(RedisConnectionFactory connectionFactory) {
		return templateBuilder.createStringTemplate(connectionFactory);
	}
} 