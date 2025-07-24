package net.causw.app.main.util;

import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
@WithMockUser
public @interface WithMockCustomUser {

	Role[] roles() default Role.NONE;

	UserState state() default UserState.AWAIT;

	AcademicStatus academicStatus() default AcademicStatus.UNDETERMINED;
}
