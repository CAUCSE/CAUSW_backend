package net.causw.app.main.shared.storage.v2.dto;

import java.time.Instant;

import lombok.Builder;

/**
 * 스토리지 업로드 결과 DTO
 * 파일 업로드 후 반환되는 정보를 담는 불변 객체
 */
@Builder
public record StorageResult(
	String fileKey,
	String fileUrl,
	Long uploadedSize,
	Instant uploadedAt) {
	public static StorageResult of(
		String fileKey,
		String fileUrl,
		Long uploadedSize,
		Instant uploadedAt) {
		return new StorageResult(
			fileKey,
			fileUrl,
			uploadedSize,
			uploadedAt);
	}
}
