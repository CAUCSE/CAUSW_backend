package net.causw.app.main.shared.storage.v2.dto;

import net.causw.app.main.domain.asset.file.enums.FilePath;

import lombok.Builder;

/**
 * 파일 메타데이터 DTO
 * 파일 업로드 및 저장에 필요한 정보를 담는 불변 객체
 */
@Builder
public record FileMetadata(
	String uuid,
	String rawFileName,
	String extension,
	String originalFileName,
	FilePath filePath,
	String fileKey) {
	public static FileMetadata of(
		String uuid,
		String rawFileName,
		String extension,
		String originalFileName,
		FilePath filePath,
		String fileKey) {
		return new FileMetadata(
			uuid,
			rawFileName,
			extension,
			originalFileName,
			filePath,
			fileKey);
	}
}
