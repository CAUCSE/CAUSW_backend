package net.causw.app.main.domain.asset.file.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 응답 (다중 파일)")
public record MultiplePresignedUrlResponse(
	@Schema(description = "각 파일의 Presigned URL 목록 (요청 순서와 동일)")
	List<PresignedUrlResponse> files) {

	public static MultiplePresignedUrlResponse of(List<PresignedUrlResponse> files) {
		return new MultiplePresignedUrlResponse(files);
	}
}