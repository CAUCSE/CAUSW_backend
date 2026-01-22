package net.causw.app.main.domain.asset.file.service.v2.util;

import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.enums.FileExtensionType;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 검증 유틸리티 클래스
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileValidator {

	/**
	 * 파일 검증 (Null, 확장자, 크기)
	 *
	 * @param file     검증할 파일
	 * @param filePath 파일 경로 타입
	 * @throws BadRequestException 검증 실패 시
	 */
	public static void validateFile(@NotNull MultipartFile file, @NotNull FilePath filePath) {
		validateFileNotNull(file);

		String extension = extractAndValidateExtension(file.getOriginalFilename());

		validateFileSize(file.getSize(), filePath);
		validateExtension(extension, filePath);
	}

	/**
	 * 파일 목록 검증 (개수, 각 파일 검증)
	 *
	 * @param fileList 검증할 파일 목록
	 * @param filePath 파일 경로 타입
	 * @throws BadRequestException 검증 실패 시
	 */
	public static void validateFileList(@NotNull List<MultipartFile> fileList, @NotNull FilePath filePath) {
		validateFileListNotEmpty(fileList);
		validateFileCount(fileList.size(), filePath);

		// 각 파일 개별 검증
		for (MultipartFile file : fileList) {
			validateFile(file, filePath);
		}
	}

	/**
	 * 확장자 검증
	 *
	 * @param extension 확장자
	 * @param filePath  파일 경로 타입
	 * @throws BadRequestException 허용되지 않은 확장자일 경우
	 */
	public static void validateExtension(String extension, FilePath filePath) {
		boolean isValidExtension = false;

		for (FileExtensionType fileExtensionType : filePath.getFileExtensionList()) {
			for (String allowedExtension : fileExtensionType.getExtensionList()) {
				if (extension.equals(allowedExtension)) {
					isValidExtension = true;
					break;
				}
			}
			if (isValidExtension) {
				break;
			}
		}

		if (!isValidExtension) {
			log.warn("Invalid file extension: {}. Allowed extensions for {}: {}",
				extension, filePath, filePath.getFileExtensionList());
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.INVALID_FILE_EXTENSION + "확장자: " + extension);
		}
	}

	/**
	 * 원본 파일명에서 확장자 추출 및 검증
	 *
	 * @param originalFilename 원본 파일명
	 * @return 확장자 (소문자)
	 * @throws BadRequestException 확장자가 없을 경우
	 */
	public static String extractAndValidateExtension(String originalFilename) {
		String extension = StringUtils.getFilenameExtension(originalFilename);
		if (extension == null || extension.isEmpty()) {
			log.warn("File extension is null or empty. Filename: {}", originalFilename);
			throw new BadRequestException(
				ErrorCode.INVALID_FILE_EXTENSION,
				MessageUtil.FILE_EXTENSION_IS_NULL);
		}
		return extension.toLowerCase();
	}

	/**
	 * 파일 크기 검증
	 *
	 * @param size     파일 크기 (bytes)
	 * @param filePath 파일 경로 타입
	 * @throws BadRequestException 크기 초과 시
	 */
	public static void validateFileSize(long size, FilePath filePath) {
		if (size > filePath.getMaxFileSize()) {
			log.warn("File size exceeded. Size: {} bytes, Max: {} bytes", size, filePath.getMaxFileSize());
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.FILE_SIZE_EXCEEDED + " (크기: " + size + " bytes)");
		}
	}

	/**
	 * 파일 개수 검증
	 *
	 * @param count    파일 개수
	 * @param filePath 파일 경로 타입
	 * @throws BadRequestException 개수 초과 시
	 */
	public static void validateFileCount(int count, FilePath filePath) {
		if (count > filePath.getMaxFileCount()) {
			log.warn("File count exceeded. Count: {}, Max: {}", count, filePath.getMaxFileCount());
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.NUMBER_OF_FILES_EXCEEDED + "파일 개수: " + count);
		}
	}

	private static void validateFileNotNull(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			log.warn("File is null or empty");
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_IS_NULL);
		}
	}

	private static void validateFileListNotEmpty(List<MultipartFile> fileList) {
		if (fileList == null || fileList.isEmpty()) {
			log.warn("File list is null or empty");
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_IS_NULL);
		}
	}
}
