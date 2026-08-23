package net.causw.app.main.core.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.causw.app.main.core.aop.enums.AdminTarget;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdminRole {
	/**
	 * 접근을 허용할 관리자 타겟을 지정합니다.
	 * 기본값은 모든 관리자가 접근 가능한 ALL_ADMIN 입니다.
	 */
	AdminTarget target() default AdminTarget.ALL_ADMIN;
}
