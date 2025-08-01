package net.causw.app.main.dto.file;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileResponseDto {
	private String originalFileName;
	private String downloadFilePath;

	public static FileResponseDto from(String filePath) {
		return FileResponseDto.builder()
			.originalFileName(Arrays.stream(URLDecoder.decode(filePath, StandardCharsets.UTF_8).split("/"))
				.reduce((a, b) -> b).orElse(null))
			.downloadFilePath(filePath)
			.build();
	}
}
