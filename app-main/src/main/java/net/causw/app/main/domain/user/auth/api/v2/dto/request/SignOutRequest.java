package net.causw.app.main.domain.user.auth.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청 DTO")
public record SignOutRequest(
	@Schema(description = "삭제할 기기의 FCM (푸시 알림) 토큰") String fcmToken) {
}
