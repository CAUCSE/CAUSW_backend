package net.causw.app.main.domain.model.enums.uuidFile;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FileExtensionType {
	IMAGE(List.of("jpg", "jpeg", "png", "gif", "bmp")),
	VIDEO(List.of("mp4", "avi", "mkv", "mov", "wmv", "flv")),
	AUDIO(List.of("mp3", "wav", "flac", "alac", "aac")),
	TEXT(List.of("txt")),
	DOCUMENT(List.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "hwp")),
	COMPRESS(List.of("zip", "rar", "alz")),
	ETC(List.of("etc"));

	private final List<String> extensionList;
}
