package net.causw.app.main.shared.exception;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import net.causw.app.main.core.global.annotation.V2Api;
import net.causw.app.main.shared.dto.ApiResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType,
		Class<? extends HttpMessageConverter<?>> converterType) {

		// 메서드에 V2Api 붙은 경우만 적용
		if (returnType.hasMethodAnnotation(V2Api.class)) {
			return true;
		}

		// 클래스에 V2Api 붙은 경우만 적용
		Class<?> declaringClass = returnType.getDeclaringClass();
		if (declaringClass.isAnnotationPresent(V2Api.class)) {
			return true;
		}

		return false;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
		ServerHttpResponse response) {
		// 이미 ResponseEntity면 그대로
		if (body instanceof ResponseEntity<?>) {
			return body;
		}

		// 이미 ApiResponse면 중복 방지
		if (body instanceof ApiResponse<?>) {
			return ResponseEntity.ok(body);
		}

		if (body instanceof String) {
			response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

			try {
				ObjectMapper om = new ObjectMapper();
				return om.writeValueAsString(ApiResponse.success(body));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}


		return ResponseEntity.ok(ApiResponse.success(body));
	}
}
