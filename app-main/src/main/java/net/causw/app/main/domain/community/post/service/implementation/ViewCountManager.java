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

	public boolean isFirstView(HttpServletRequest request, HttpServletResponse response, String postId) {
		String cookieName = COOKIE_PREFIX + postId;

		if (request.getCookies() != null) {
			boolean isViewed = Arrays.stream(request.getCookies())
				.anyMatch(cookie -> cookie.getName().equals(cookieName));
			if (isViewed) {
				return false;
			}
		}

		Cookie viewCookie = new Cookie(cookieName, "true");
		viewCookie.setMaxAge(COOKIE_MAX_AGE);
		viewCookie.setPath("/");
		response.addCookie(viewCookie);

		return true;
	}
}