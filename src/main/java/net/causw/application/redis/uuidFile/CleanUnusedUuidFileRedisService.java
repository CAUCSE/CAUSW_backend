package net.causw.application.redis.uuidFile;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CleanUnusedUuidFileRedisService {

    @Qualifier("cleanUnusedUuidFileRedisTemplate")
    private final RedisTemplate<String, String> cleanUnusedUuidFileRedisTemplate;

    /** UuidFile 정리 배치 작업 관련 Redis 메서드
    * 1. PageNum
    * Key: {tableName}PageNum
    * Value: {PageNum}
     */
    public void setPageNumData(String tableName, Integer pageNum, Long expiredTime) {
        String redisKey = tableName + "PageNum";
        cleanUnusedUuidFileRedisTemplate.opsForValue().set(redisKey, String.valueOf(pageNum), expiredTime, TimeUnit.MILLISECONDS);
    }

    public Integer getPageNumData(String tableName) {
        String redisKey = tableName + "PageNum";
        String value = cleanUnusedUuidFileRedisTemplate.opsForValue().get(redisKey);
        return value != null ? Integer.valueOf(value) : null;
    }
} 