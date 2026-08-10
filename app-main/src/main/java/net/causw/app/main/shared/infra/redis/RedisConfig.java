package net.causw.app.main.shared.infra.redis;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	@Bean
	public RedisSerializer<Object> redisValueSerializer() {
		return RedisSerializer.json();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(
		RedisConnectionFactory connectionFactory,
		RedisSerializer<Object> redisValueSerializer) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		StringRedisSerializer stringSerializer = new StringRedisSerializer();

		// Key는 String으로 직렬화
		redisTemplate.setKeySerializer(stringSerializer);

		// Value는 다양한 타입을 처리할 수 있도록 JSON 직렬화
		redisTemplate.setValueSerializer(redisValueSerializer);

		// HashKey와 HashValue의 직렬화 설정
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(redisValueSerializer);

		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	// CacheManager 설정
	@Bean
	public CacheManager cacheManager(
		RedisConnectionFactory connectionFactory,
		RedisSerializer<Object> redisValueSerializer) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer))
			.entryTtl(Duration.ofMinutes(60)); // 캐시 TTL 설정, (기본값: 1시간, TTL 따로 지정안한 경우만 적용)

		return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
			.cacheDefaults(redisCacheConfiguration)
			.build();
	}

}
