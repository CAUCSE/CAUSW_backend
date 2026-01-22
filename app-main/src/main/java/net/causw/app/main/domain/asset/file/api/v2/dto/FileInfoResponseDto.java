package net.causw.app.main.domain.asset.file.api.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;

import lombok.Builder;

@Builder
public record FileInfoResponseDto(
	String fileId,
	String fileUrl,
	String originalFileName,
	String extension,
	FilePath filePath,
	Boolean isUsed,
	LocalDateTime createdAt,
	LocalDateTime updatedAt) {
	public static FileInfoResponseDto from(UuidFile uuidFile) {
		return FileInfoResponseDto.builder()
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
