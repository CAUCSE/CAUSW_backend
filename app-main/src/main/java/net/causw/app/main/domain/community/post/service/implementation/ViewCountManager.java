package net.causw.app.main.domain.community.post.service.implementation;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ViewCountManager {

	private static final String COOKIE_PREFIX = "post_view_";
	private static final int COOKIE_MAX_AGE = 60 * 60 * 24; // 24시간

	/**
	 * 해당 게시글에 대한 조회수 쿠키가 이미 존재하는지 확인합니다. (권한 검증 전 사용)
	 */
	public boolean hasViewedCookie(HttpServletRequest request, String postId) {
		String cookieName = COOKIE_PREFIX + postId;

		if (request.getCookies() != null) {
			return Arrays.stream(request.getCookies())
				.anyMatch(cookie -> cookie.getName().equals(cookieName));
		}

		return false;
	}

	/**
	 * 모든 권한 검증과 조회수 증가가 성공적으로 끝난 후, 클라이언트에게 조회 완료 쿠키를 발급합니다.
	 */
	public void markViewed(HttpServletResponse response, String postId) {
		String cookieName = COOKIE_PREFIX + postId;

		Cookie viewCookie = new Cookie(cookieName, "true");
		viewCookie.setMaxAge(COOKIE_MAX_AGE);
		viewCookie.setPath("/");
		response.addCookie(viewCookie);
	}
}