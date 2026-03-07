package net.causw.app.main.domain.asset.file.api.v2.dto.response;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

import lombok.Builder;

@Builder
public record FileUploadResponse(
	String fileId,
	String fileUrl,
	String originalFileName,
	String extension) {
	public static FileUploadResponse from(UuidFile uuidFile) {
		return FileUploadResponse.builder()
			.fileId(uuidFile.getId())
			.fileUrl(uuidFile.getFileUrl())
			.originalFileName(uuidFile.getRawFileName())
			.extension(uuidFile.getExtension())
			.build();
	}
}
