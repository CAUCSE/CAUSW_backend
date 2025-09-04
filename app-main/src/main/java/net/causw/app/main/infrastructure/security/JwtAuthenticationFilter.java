package net.causw.app.main.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.causw.global.exception.UnauthorizedException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.constant.MessageUtil;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest request,
		@NotNull HttpServletResponse response,
		@NotNull FilterChain chain) throws ServletException, IOException {
		String token = jwtTokenProvider.resolveToken(request);

		if (StringUtils.hasText(token)) {
			try {
				if (jwtTokenProvider.validateToken(token)) {
					Authentication auth = jwtTokenProvider.getAuthentication(token);
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (UnauthorizedException exception) {
				SecurityContextHolder.clearContext();
				request.setAttribute("exception", exception);

				throw exception;
			}
		}

		chain.doFilter(request, response);
	}
}
