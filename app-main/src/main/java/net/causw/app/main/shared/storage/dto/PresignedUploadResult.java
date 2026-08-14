package net.causw.app.main.shared.storage.dto;

import java.time.Instant;

/**
 * Presigned URL 발급 결과 DTO
 *
 * @param presignedUrl 클라이언트가 파일을 직접 PUT 업로드할 서명된 S3 URL
 * @param fileUrl      업로드 완료 후 파일에 접근할 최종 공개 URL
 * @param expiresAt    presignedUrl 만료 시각
 */
public record PresignedUploadResult(
	String presignedUrl,
	String fileUrl,
	Instant expiresAt) {

	public static PresignedUploadResult of(String presignedUrl, String fileUrl, Instant expiresAt) {
		return new PresignedUploadResult(presignedUrl, fileUrl, expiresAt);
	}
}