package net.causw.app.main.domain.asset.file.api.v2.dto.request;

import net.causw.app.main.domain.asset.file.enums.FilePath;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Presigned URL 발급 요청 (단일 파일)")
public record PresignedUrlRequest(
	@NotBlank(message = "파일명을 입력해 주세요.")
	@Schema(description = "업로드할 파일명 (확장자 포함)", example = "image.jpg")
	String fileName,

	@NotNull(message = "파일 크기를 입력해 주세요.")
	@Positive(message = "파일 크기는 0보다 커야 합니다.")
	@Schema(description = "파일 크기 (bytes)", example = "1048576")
	Long fileSize,

	@NotNull(message = "파일 경로 타입을 선택해 주세요.")
	@Schema(description = "파일 경로 타입", example = "POST")
	FilePath filePath) {
}