package net.causw.domain.validation.valid;

import net.causw.domain.model.enums.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserValid {
    boolean UserRolesIsNoneValidator() default true;

    boolean UserStateValidator() default true;

    boolean UserRoleValidator() default false;
    Role[] targetRoleSet() default {};

    boolean UserRoleWithoutAdminValidator() default false;

    boolean UserStateIsDropOrIsInActiveValidator() default false;

    boolean UserStateIsNotDropAndActiveValidator() default false;

    boolean StudentIsNullValidator() default false;

    boolean AdmissionYearValidator() default false;
}
