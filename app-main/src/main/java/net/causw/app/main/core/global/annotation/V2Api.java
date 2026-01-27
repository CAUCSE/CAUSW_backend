package net.causw.app.main.core.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * v1 API 컨트롤러를 표시하는 마커 어노테이션
 * GlobalExceptionHandler에서 v1 API만 처리하기 위해 사용됩니다.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface V2Api {
}
