package net.causw.app.main.domain.asset.file.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;

import lombok.Builder;

@Builder
public record FileInfoResponse(
	String fileId,
	String fileUrl,
	String originalFileName,
	String extension,
	FilePath filePath,
	Boolean isUsed,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {
	public static FileInfoResponse from(UuidFile uuidFile) {
		return FileInfoResponse.builder()
			.fileId(uuidFile.getId())
			.fileUrl(uuidFile.getFileUrl())
			.originalFileName(uuidFile.getRawFileName())
			.extension(uuidFile.getExtension())
			.filePath(uuidFile.getFilePath())
			.isUsed(uuidFile.getIsUsed())
			.createdAt(uuidFile.getCreatedAt())
			.updatedAt(uuidFile.getUpdatedAt())
			.build();
	}
}
