package net.causw.app.main.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private boolean success;
	private T data;
	private String error;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> failure(String error) {
		return new ApiResponse<>(false, null, error);
	}

	public static <T> ApiResponse<T> ofDefault() {
		return new ApiResponse<>(true, null, null);
	}
}
