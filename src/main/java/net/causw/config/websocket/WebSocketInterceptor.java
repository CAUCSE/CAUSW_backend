package net.causw.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.config.security.JwtTokenProvider;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // WebSocket 연결 시 JWT 토큰 검증
        if (accessor.getCommand() == StompCommand.CONNECT) {
            handleConnect(accessor);
        }
        
        return message;
    }

        private void handleConnect(StompHeaderAccessor accessor) {
        // Authorization 헤더에서 JWT 토큰 추출
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        String token = jwtTokenProvider.resolveToken(authHeader);
        
        if (token == null) {
            throw new UnauthorizedException(ErrorCode.INVALID_JWT, MessageUtil.INVALID_TOKEN);
        }
        
        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorCode.INVALID_JWT, MessageUtil.INVALID_TOKEN);
        }
        
        // 사용자 인증 정보 생성 및 세션에 저장
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        accessor.setUser(authentication);
    }
}