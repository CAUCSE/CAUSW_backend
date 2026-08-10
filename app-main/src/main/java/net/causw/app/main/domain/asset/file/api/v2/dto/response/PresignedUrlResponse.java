package net.causw.app.main.domain.asset.file.api.v2.dto.response;

import java.time.Instant;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.shared.storage.dto.PresignedUploadResult;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Presigned URL 발급 응답")
public record PresignedUrlResponse(
	@Schema(description = "파일 UUID (도메인 생성 요청 시 사용)", example = "550e8400-e29b-41d4-a716-446655440000") String uuid,

	@Schema(description = "S3 파일 키", example = "post/image_550e8400.jpg") String fileKey,

	@Schema(description = "클라이언트가 PUT 요청을 보낼 Presigned URL") String presignedUrl,

	@Schema(description = "업로드 완료 후 파일에 접근할 최종 URL") String fileUrl,

	@Schema(description = "Presigned URL 만료 시각") Instant expiresAt) {

	public static PresignedUrlResponse of(UuidFile uuidFile, PresignedUploadResult presignedResult) {
		return PresignedUrlResponse.builder()
			.uuid(uuidFile.getUuid())
			.fileKey(uuidFile.getFileKey())
			.presignedUrl(presignedResult.presignedUrl())
			.fileUrl(presignedResult.fileUrl())
			.expiresAt(presignedResult.expiresAt())
			.build();
	}
}