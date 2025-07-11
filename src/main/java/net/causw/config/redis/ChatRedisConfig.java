package net.causw.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class ChatRedisConfig {
    
    @Autowired
    private RedisTemplateBuilder templateBuilder;
    
    // 채팅 전용 RedisTemplate(안 읽은 수, 캐싱)
    @Bean("chatRedisTemplate")
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        return templateBuilder.createObjectTemplate(connectionFactory);
    }
    
    // 채팅 세션 전용 RedisTemplate
    @Bean("chatSessionRedisTemplate")
    public RedisTemplate<String, String> chatSessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        return templateBuilder.createStringTemplate(connectionFactory);
    }
} 