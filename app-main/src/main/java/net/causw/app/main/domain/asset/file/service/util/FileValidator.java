package net.causw.app.main.domain.asset.file.service.util;

import java.util.List;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.enums.FileExtensionType;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.FileErrorCode;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

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
	 * 파일명 + 크기 기반 메타데이터 검증 (Presigned URL 발급 시 사용)
	 *
	 * @param fileName 원본 파일명
	 * @param fileSize 파일 크기 (bytes)
	 * @param filePath 파일 경로 타입
	 * @throws BadRequestException 검증 실패 시
	 */
	public static void validateUploadRequest(String fileName, long fileSize, @NotNull FilePath filePath,
		String contentType) {
		String extension = extractAndValidateExtension(fileName);
		validateFileSize(fileSize, filePath);
		validateExtension(extension, filePath);
		validateContentType(extension, contentType);
	}

	/**
	 * 파일 메타데이터 목록 검증 (다중 Presigned URL 발급 시 사용)
	 *
	 * @param fileNames 원본 파일명 목록
	 * @param fileSizes 파일 크기 목록 (bytes)
	 * @param filePath  파일 경로 타입
	 * @throws BaseRunTimeV2Exception 파일 목록이 비어있을 경우 (FILE_LIST_EMPTY)
	 * @throws BadRequestException 확장자·크기·개수 검증 실패 시
	 */
	public static void validateUploadRequests(
		@NotNull List<String> fileNames,
		@NotNull List<Long> fileSizes,
		@NotNull List<String> contentTypes,
		@NotNull FilePath filePath) {
		if (fileNames.isEmpty()) {
			throw FileErrorCode.FILE_LIST_EMPTY.toBaseException();
		}
		validateFileCount(fileNames.size(), filePath);
		for (int i = 0; i < fileNames.size(); i++) {
			validateUploadRequest(fileNames.get(i), fileSizes.get(i), filePath, contentTypes.get(i));
		}
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
	 * Content-Type과 확장자 일치 여부 검증
	 * 1차 타입(image/video/application 등)이 일치하지 않으면 거부합니다.
	 *
	 * @param extension   파일 확장자 (소문자)
	 * @param contentType 클라이언트 제공 MIME 타입
	 * @throws BadRequestException Content-Type이 유효하지 않거나 확장자와 불일치할 경우
	 */
	public static void validateContentType(String extension, String contentType) {
		MediaType provided;
		try {
			provided = MediaType.parseMediaType(contentType);
		} catch (InvalidMediaTypeException e) {
			log.warn("Invalid Content-Type format: {}", contentType);
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
				"유효하지 않은 Content-Type입니다: " + contentType);
		}

		MediaTypeFactory.getMediaType("file." + extension).ifPresent(expected -> {
			if (!provided.getType().equals(expected.getType())) {
				log.warn("Content-Type mismatch. Extension: {}, Expected primary type: {}, Provided: {}",
					extension, expected.getType(), contentType);
				throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
					"Content-Type이 파일 형식과 일치하지 않습니다. 제공된 Content-Type: " + contentType);
			}
		});
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
		if (size <= 0 || size > filePath.getMaxFileSize()) {
			log.warn("File size invalid. Size: {} bytes, Max: {} bytes", size, filePath.getMaxFileSize());
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
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_IS_NULL);
		}
	}
}
