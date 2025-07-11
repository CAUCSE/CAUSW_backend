package net.causw.application.redis.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRedisService {

    @Qualifier("chatRedisTemplate")
    private final RedisTemplate<String, Object> chatRedisTemplate;
    
    @Qualifier("chatSessionRedisTemplate")
    private final RedisTemplate<String, String> chatSessionRedisTemplate;

    /** 채팅 관련 Redis 메서드
    * 1. UnreadCount
    * Key: unread:room:{userId}:{roomId}
    * Value: {안 읽은 메시지 수}
    *
    * 2. TotalUnreadCount
    * Key: total_unread:{userId}
    * Value: {전체 안 읽은 메시지 수}
    *
    * 3. Session
    * Key: session:{userId}
    * Value: {세션 정보}
    *
    * 4. Cache
    * Key: cache:{key}
    * Value: {캐시 데이터}
     */

    public void incrementUnreadCount(String userId, String roomId) {
        String roomKey = "unread:room:" + userId + ":" + roomId;
        String totalKey = "total_unread:" + userId;
        chatRedisTemplate.opsForValue().increment(roomKey);
        chatRedisTemplate.opsForValue().increment(totalKey);
    }

    public void clearUnreadCount(String userId, String roomId) {
        String roomKey = "unread:room:" + userId + ":" + roomId;
        Object unreadCount = chatRedisTemplate.opsForValue().get(roomKey);
        if (unreadCount != null) {
            Integer count = Integer.valueOf(unreadCount.toString());
            chatRedisTemplate.delete(roomKey);
            String totalKey = "total_unread:" + userId;
            chatRedisTemplate.opsForValue().decrement(totalKey, count);
        }
    }

    public Integer getUnreadCount(String userId, String roomId) {
        String roomKey = "unread:room:" + userId + ":" + roomId;
        Object count = chatRedisTemplate.opsForValue().get(roomKey);
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }

    public Integer getTotalUnreadCount(String userId) {
        String totalKey = "total_unread:" + userId;
        Object count = chatRedisTemplate.opsForValue().get(totalKey);
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }

    public void setSession(String userId, String sessionInfo, Long expiredTime) {
        String redisKey = "session:" + userId;
        chatSessionRedisTemplate.opsForValue().set(redisKey, sessionInfo, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getSession(String userId) {
        String redisKey = "session:" + userId;
        return chatSessionRedisTemplate.opsForValue().get(redisKey);
    }

    public void deleteSession(String userId) {
        String redisKey = "session:" + userId;
        chatSessionRedisTemplate.delete(redisKey);
    }

    public void setCacheData(String key, Object data, Long expiredTime) {
        String cacheKey = "cache:" + key;
        chatRedisTemplate.opsForValue().set(cacheKey, data, expiredTime, TimeUnit.MILLISECONDS);
    }

    public Object getCacheData(String key) {
        String cacheKey = "cache:" + key;
        return chatRedisTemplate.opsForValue().get(cacheKey);
    }

    public void deleteCacheData(String key) {
        String cacheKey = "cache:" + key;
        chatRedisTemplate.delete(cacheKey);
    }
} 