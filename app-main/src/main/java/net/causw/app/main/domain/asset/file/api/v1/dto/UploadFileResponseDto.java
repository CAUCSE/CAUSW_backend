package net.causw.app.main.domain.asset.file.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UploadFileResponseDto {
	private String path;

	public static UploadFileResponseDto from(String path) {
		return UploadFileResponseDto.builder()
			.path(path)
			.build();
	}
}
