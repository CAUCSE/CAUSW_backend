package net.causw.app.main.domain.asset.file.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.asset.file.enums.FilePath;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Presigned URL 발급 요청 (다중 파일)")
public record MultiplePresignedUrlRequest(
	@NotEmpty(message = "파일 목록을 입력해 주세요.") @Valid @Schema(description = "업로드할 파일 목록") List<FileEntry> files,

	@NotNull(message = "파일 경로 타입을 선택해 주세요.") @Schema(description = "파일 경로 타입", example = "POST") FilePath filePath) {

	@Schema(description = "개별 파일 정보")
	public record FileEntry(
		@Schema(description = "파일명 (확장자 포함)", example = "image.jpg") String fileName,

		@Schema(description = "파일 크기 (bytes)", example = "1048576") Long fileSize) {
	}
}