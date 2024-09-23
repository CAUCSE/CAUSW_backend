package net.causw.domain.model.util;

import net.causw.application.dto.form.FormResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /* 로그인 관련 Redis 메서드
    * 1. RefreshToken
    * Key: RefreshToken:{RefreshToken 값}
    * Value: {User ID}
    *
    * 2. Blacklist
    * Key: Blacklist:{AccessToken 값}
    * Value: "BLACKLISTED"
     */
    public void setRefreshTokenData(String key, String value, Long expiredTime){
        String redisKey = "RefreshToken:" + key;
        redisTemplate.opsForValue().set(redisKey, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshTokenData(String key){
        String redisKey = "RefreshToken:" + key;
        return (String) redisTemplate.opsForValue().get(redisKey);
    }

    public void deleteRefreshTokenData(String key){
        String redisKey = "RefreshToken:" + key;
        redisTemplate.delete(redisKey);
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
        String redisKey = "Blacklist" + token;
        redisTemplate.opsForValue().set(redisKey, "BLACKLISTED", StaticValue.JWT_ACCESS_TOKEN_VALID_TIME, TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        String redisKey = "Blacklist" + token;
        return "BLACKLISTED".equals(redisTemplate.opsForValue().get(redisKey));
    }

    /* UuidFile 관련 Redis 메서드
    * 1. PageNum
    * Key: {tableName}PageNum
    * Value: {PageNum}
     */
    public void setPageNumData(String tableName, Integer pageNum, Long expiredTime) {
        String redisKey = tableName + "PageNum";
        redisTemplate.opsForValue().set(redisKey, pageNum, expiredTime, TimeUnit.MILLISECONDS);
    }

    public Integer getPageNumData(String tableName) {
        String redisKey = tableName + "PageNum";
        return (Integer) redisTemplate.opsForValue().get(redisKey);
    }

}
