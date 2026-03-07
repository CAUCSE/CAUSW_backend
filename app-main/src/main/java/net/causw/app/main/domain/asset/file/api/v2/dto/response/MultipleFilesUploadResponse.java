package net.causw.app.main.domain.asset.file.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

import lombok.Builder;

@Builder
public record MultipleFilesUploadResponse(
	List<FileUploadResponse> files,
	Integer totalCount) {
	public static MultipleFilesUploadResponse from(List<UuidFile> uuidFiles) {
		List<FileUploadResponse> fileDtos = uuidFiles.stream()
			.map(FileUploadResponse::from)
			.toList();

		return MultipleFilesUploadResponse.builder()
			.files(fileDtos)
			.totalCount(fileDtos.size())
			.build();
	}
}
