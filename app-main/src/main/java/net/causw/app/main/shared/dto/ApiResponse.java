package net.causw.app.main.shared.dto;

import net.causw.app.main.shared.exception.ResponseCode;
import net.causw.app.main.shared.exception.BaseResponseCode;
import net.causw.app.main.shared.exception.GlobalErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	@Schema(description = "응답 코드", example = "S000")
	private String code;

	@Schema(description = "응답 메시지", example = "요청 처리 성공")
	private String message;

	@Schema(description = "실제 응답 데이터(페이로드)")
	private T data;

	/**
	 * 공통 성공 응답 생성
	 * @param data
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(
			ResponseCode.SUCCESS.getCode(),
			ResponseCode.SUCCESS.getMessage(),
			data
		);
	}

	/**
	 * 공통 성공 응답 생성 (내부 data 없음)
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> success() {
		return ApiResponse.success(null);
	}

	/**
	 * 공통 에러 응답 생성
	 * @param errorCode
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> error(BaseResponseCode errorCode) {
		return new ApiResponse<>(
			errorCode.getCode(),
			errorCode.getMessage(),
			null
		);
	}

	/**
	 * 커스텀 에러 응답 생성
	 * @param code
	 * @param message
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}

	/**
	 * 내부 서버 에러 응답 생성
	 * @param message
	 * @return
	 * @param <T>
	 */
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(
			GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
			message,
			null
		);
	}
}
