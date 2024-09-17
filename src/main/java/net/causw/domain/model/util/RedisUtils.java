package net.causw.domain.model.util;

import net.causw.application.dto.form.FormResponseDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setData(String key, String value,Long expiredTime){
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getData(String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key){
        redisTemplate.delete(key);
    }

    public void setCacheData(String key, FormResponseDto value, Long expiredTime) {
        redisTemplate.opsForValue().set("form:" + key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public Object getCacheData(String key) {
        return redisTemplate.opsForValue().get("form:" + key);
    }

    public void deleteCacheData(String key) {
        redisTemplate.delete("form:" + key);
    }

    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, "BLACKLISTED", StaticValue.JWT_ACCESS_TOKEN_VALID_TIME, TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        return "BLACKLISTED".equals(redisTemplate.opsForValue().get(token));
    }
}
