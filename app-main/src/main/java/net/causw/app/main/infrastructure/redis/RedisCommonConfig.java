package net.causw.app.main.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCommonConfig {

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.host}")
	private String host;

	// Redis ConnectionFactory 생성 (기존 방식과 동일)
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(host, port);
	}

	// 공통 RedisTemplate 생성
	@Bean("commonRedisTemplate")
	public RedisTemplate<String, Object> commonRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Key는 String으로 직렬화
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// Value는 다양한 타입을 처리할 수 있도록 JSON 직렬화
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		template.afterPropertiesSet();
		return template;
	}

	// String 전용 RedisTemplate 생성
	@Bean("commonStringRedisTemplate")
	public RedisTemplate<String, String> commonStringRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(stringSerializer);

		template.afterPropertiesSet();
		return template;
	}

	// CacheManager 설정 (향후 @Cacheable 어노테이션 사용을 위한 준비)
	@Bean("redisCacheManager")
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
			.entryTtl(Duration.ofMinutes(60));  // 캐시 TTL 설정 (기본값: 1시간, TTL 따로 지정안한 경우만 적용)

		return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
			.cacheDefaults(redisCacheConfiguration)
			.build();
	}
} 