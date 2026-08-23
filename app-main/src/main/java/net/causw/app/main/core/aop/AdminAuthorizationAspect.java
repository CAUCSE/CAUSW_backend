package net.causw.app.main.core.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.global.constant.MessageUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class AdminAuthorizationAspect {

	@Before("@annotation(net.causw.app.main.core.aop.annotation.RequireAdminRole) || @within(net.causw.app.main.core.aop.annotation.RequireAdminRole)")
	public void checkAdminAuthorization(JoinPoint joinPoint) {
		// SecurityContext에서 인증 정보 획득
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new AccessDeniedException(MessageUtil.ACCESS_DENIED);
		}
		Object principal = auth.getPrincipal();
		if (!(principal instanceof CustomUserDetails)) {
			throw new AccessDeniedException(MessageUtil.ACCESS_DENIED);
		}

		// User 정보 추출
		CustomUserDetails userDetails = (CustomUserDetails) principal;
		User user = userDetails.getUser();

		boolean isSystemAdmin = user.getRoles().contains(Role.SYSTEM_ADMIN);
		boolean isAdmin = user.getRoles().contains(Role.ADMIN);
		AcademicStatus academicStatus = user.getAcademicStatus();

		// 실행대상의 요구 권한 정보 추출
		RequireAdminRole requireAdminRole = getAnnotation(joinPoint);
		AdminTarget target = requireAdminRole.target();

		// 권한 검증
		boolean isAuthorized = false;
		if (isSystemAdmin) {
			isAuthorized = true; // SYSTEM_ADMIN은 모든 권한 허용
		} else if (isAdmin) {
			switch (target) {
				case ALL_ADMIN:
					isAuthorized = true; // 모든 ADMIN 허용
					break;
				case ENROLLED_ADMIN:
					isAuthorized = academicStatus == AcademicStatus.ENROLLED; // 재학생 관리자만 허용
					break;
				case GRADUATED_ADMIN:
					isAuthorized = academicStatus == AcademicStatus.GRADUATED; // 졸업생 관리자만 허용
					break;
				case SYSTEM_ONLY:
					isAuthorized = false; // 기타 경우는 접근 불가
					break;
			}
		}

		if (!isAuthorized) {
			throw new AccessDeniedException(MessageUtil.ACCESS_DENIED);
		}
	}

	// JoinPoint에서 @RequireAdminRole 어노테이션 추출
	private RequireAdminRole getAnnotation(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		RequireAdminRole annotation = AnnotationUtils.findAnnotation(method, RequireAdminRole.class);
		if (annotation == null) {
			annotation = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequireAdminRole.class);
		}

		return annotation;
	}

}
