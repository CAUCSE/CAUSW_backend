package net.causw.app.main.shared.infra.firebase;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.shared.infra.redis.RedisUtils;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

/**
 * V2 이후 FCM 토큰 CRUD 단일 진입점.
 * FcmUtils를 대체하며, 동시수정 방어와 auto-save를 포함한다.
 */
@RequiredArgsConstructor
@Component
public class FcmTokenManager {

    private final RedisUtils redisUtils;
    private final UserRepository userRepository;

    public void addFcmToken(User user, String refreshToken, String fcmToken) {
        if (!redisUtils.existsFcmToken(fcmToken)) {
            user.getFcmTokens().add(fcmToken);
            redisUtils.setFcmTokenData(fcmToken, refreshToken, StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);
            userRepository.save(user);
        }
    }

    public void removeFcmToken(User user, String fcmToken) {
        user.removeFcmToken(fcmToken);
        redisUtils.deleteFcmTokenData(fcmToken);
        userRepository.save(user);
    }

    public void cleanInvalidFcmTokens(User user) {
        Set<String> copy = new HashSet<>(user.getFcmTokens());
        for (String fcmToken : copy) {
            if (!redisUtils.existsFcmToken(fcmToken)) {
                user.removeFcmToken(fcmToken);
            } else {
                String refreshToken = redisUtils.getFcmTokenData(fcmToken);
                if (refreshToken == null || !redisUtils.existsRefreshToken(refreshToken)) {
                    user.removeFcmToken(fcmToken);
                    redisUtils.deleteFcmTokenData(fcmToken);
                }
            }
        }
        userRepository.save(user);
    }

    public void clearFcmTokens(User user) {
        if (user.getFcmTokens() == null || user.getFcmTokens().isEmpty()) {
            return;
        }
        for (String token : user.getFcmTokens()) {
            redisUtils.deleteFcmTokenData(token);
        }
        user.getFcmTokens().clear();
        userRepository.save(user);
    }
}