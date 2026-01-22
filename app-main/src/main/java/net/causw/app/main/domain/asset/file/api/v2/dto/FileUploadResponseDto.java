package net.causw.app.main.domain.asset.file.api.v2.dto;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

import lombok.Builder;

@Builder
public record FileUploadResponseDto(
	String fileId,
	String fileUrl,
	String originalFileName,
	String extension) {
	public static FileUploadResponseDto from(UuidFile uuidFile) {
		return FileUploadResponseDto.builder()
			.fileId(uuidFile.getId())
			.fileUrl(uuidFile.getFileUrl())
			.originalFileName(uuidFile.getRawFileName())
			.extension(uuidFile.getExtension())
			.build();
	}
}
