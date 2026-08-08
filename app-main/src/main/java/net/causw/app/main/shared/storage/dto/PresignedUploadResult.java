package net.causw.app.main.shared.storage.dto;

import java.time.Instant;

public record PresignedUploadResult(
	String presignedUrl,
	String fileUrl,
	Instant expiresAt) {

	public static PresignedUploadResult of(String presignedUrl, String fileUrl, Instant expiresAt) {
		return new PresignedUploadResult(presignedUrl, fileUrl, expiresAt);
	}
}