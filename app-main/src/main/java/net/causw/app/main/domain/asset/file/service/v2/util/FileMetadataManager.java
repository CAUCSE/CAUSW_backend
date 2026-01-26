package net.causw.app.main.domain.asset.file.service.v2.util;

import java.util.UUID;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 메타데이터 생성 및 처리 유틸리티 클래스
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileMetadataManager {

	/**
	 * MultipartFile로부터 FileMetadata 생성
	 *
	 * @param file     업로드된 파일
	 * @param filePath 파일 경로 타입
	 * @return 파일 메타데이터
	 */
	public static FileMetadata createMetadata(@NotNull MultipartFile file, @NotNull FilePath filePath) {
		String uuid = generateUuid();
		String originalFileName = file.getOriginalFilename();
		String rawFileName = extractRawFileName(originalFileName);
		String extension = extractExtension(originalFileName);
		String fileKey = buildFileKey(uuid, rawFileName, extension, filePath);

		log.debug("Created file metadata. UUID: {}, FileName: {}, Extension: {}, FileKey: {}",
			uuid, rawFileName, extension, fileKey);

		return FileMetadata.of(
			uuid,
			rawFileName,
			extension,
			originalFileName,
			filePath,
			fileKey);
	}

	/**
	 * 원본 파일명에서 확장자를 제외한 파일명 추출
	 *
	 * @param originalFileName 원본 파일명
	 * @return 확장자 제외 파일명
	 * @throws BadRequestException 파일명이 null일 경우
	 */
	public static String extractRawFileName(@NotBlank String originalFileName) {
		if (originalFileName == null || originalFileName.isEmpty()) {
			log.warn("Original filename is null or empty");
			throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.FILE_NAME_IS_NULL);
		}
		return StringUtils.stripFilenameExtension(originalFileName);
	}

	/**
	 * 원본 파일명에서 확장자 추출
	 *
	 * @param originalFileName 원본 파일명
	 * @return 확장자 (소문자)
	 * @throws BadRequestException 확장자가 null일 경우
	 */
	public static String extractExtension(@NotBlank String originalFileName) {
		String extension = StringUtils.getFilenameExtension(originalFileName);
		if (extension == null || extension.isEmpty()) {
			log.warn("File extension is null or empty. Filename: {}", originalFileName);
			throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.FILE_EXTENSION_IS_NULL);
		}
		return extension.toLowerCase();
	}

	/**
	 * S3 파일 키 생성
	 * 형식: {directory}/{rawFileName}_{uuid}.{extension}
	 *
	 * @param uuid        UUID
	 * @param rawFileName 파일명
	 * @param extension   확장자
	 * @param filePath    파일 경로 타입
	 * @return 파일 키
	 */
	public static String buildFileKey(
		@NotBlank String uuid,
		@NotBlank String rawFileName,
		@NotBlank String extension,
		@NotNull FilePath filePath) {
		return filePath.getDirectory() + "/" + rawFileName + "_" + uuid + "." + extension;
	}

	private static String generateUuid() {
		return UUID.randomUUID().toString();
	}
}
