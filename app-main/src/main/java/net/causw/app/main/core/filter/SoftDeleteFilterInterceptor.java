package net.causw.app.main.core.filter;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SoftDeleteFilterInterceptor implements HandlerInterceptor {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		entityManager.unwrap(Session.class).enableFilter("softDelete");
		return true;
	}
}
