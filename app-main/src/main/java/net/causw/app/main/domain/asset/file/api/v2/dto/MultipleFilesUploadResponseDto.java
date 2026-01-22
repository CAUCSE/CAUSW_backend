package net.causw.app.main.domain.asset.file.api.v2.dto;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

import lombok.Builder;

@Builder
public record MultipleFilesUploadResponseDto(
	List<FileUploadResponseDto> files,
	Integer totalCount) {
	public static MultipleFilesUploadResponseDto from(List<UuidFile> uuidFiles) {
		List<FileUploadResponseDto> fileDtos = uuidFiles.stream()
			.map(FileUploadResponseDto::from)
			.toList();

		return MultipleFilesUploadResponseDto.builder()
			.files(fileDtos)
			.totalCount(fileDtos.size())
			.build();
	}
}
