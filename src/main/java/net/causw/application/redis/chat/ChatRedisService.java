package net.causw.application.redis.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatRedisService {

    private static final long SESSION_TTL_MS = 60 * 1000L;

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
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    public Integer getTotalUnreadCount(String userId) {
        String totalKey = "total_unread:" + userId;
        Object count = chatRedisTemplate.opsForValue().get(totalKey);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    public void setSession(String userId, String roomId) {
        String redisKey = "session:" + userId + ":" + roomId;
        chatSessionRedisTemplate.opsForValue().set(redisKey, "subscribed", SESSION_TTL_MS, TimeUnit.MILLISECONDS);
    }

    public String getSession(String userId, String roomId) {
        String redisKey = "session:" + userId + ":" + roomId;
        return chatSessionRedisTemplate.opsForValue().get(redisKey);
    }

    public void deleteSession(String userId, String roomId) {
        String redisKey = "session:" + userId  + ":" + roomId;
        chatSessionRedisTemplate.delete(redisKey);
    }

    public void addSessionReverseMapping(String sessionId, String userId, String roomId) {
        String key = "session_reverse:" + sessionId;
        String value = userId + ":" + roomId;
        chatSessionRedisTemplate.opsForSet().add(key, value);
        chatSessionRedisTemplate.expire(key, SESSION_TTL_MS, TimeUnit.MILLISECONDS);
    }

    public Set<String> getSessionReverseMappings(String sessionId) {
        String key = "session_reverse:" + sessionId;
        Set<String> values = chatSessionRedisTemplate.opsForSet().members(key);
        return values != null ? values : Collections.emptySet();
    }

    public void deleteSessionReverse(String sessionId) {
        String key = "session_reverse:" + sessionId;
        chatSessionRedisTemplate.delete(key);
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