package net.causw.app.main.domain.asset.file.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.asset.file.api.v2.dto.request.MultiplePresignedUrlRequest;
import net.causw.app.main.domain.asset.file.api.v2.dto.request.PresignedUrlRequest;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.MultiplePresignedUrlResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.PresignedUrlResponse;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.util.FileMetadataManager;
import net.causw.app.main.domain.asset.file.service.util.FileValidator;
import net.causw.app.main.shared.exception.errorcode.FileErrorCode;
import net.causw.app.main.shared.storage.StorageClient;
import net.causw.app.main.shared.storage.dto.FileMetadata;
import net.causw.app.main.shared.storage.dto.PresignedUploadResult;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 관리 서비스 V2
 */
@Slf4j
@MeasureTime
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UuidFileService {

	private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofMinutes(10);

	private final FileReader fileReader;
	private final FileWriter fileWriter;
	private final StorageClient storageClient;

	/**
	 * 단일 파일 Presigned URL 발급
	 * 파일 메타데이터를 검증하고 PENDING 상태의 UuidFile을 생성한 뒤 S3 presigned PUT URL을 반환합니다.
	 *
	 * @param request 파일명, 파일 크기, 파일 경로 타입
	 * @return presigned URL, 최종 파일 URL, UUID 등
	 */
	@Transactional
	public PresignedUrlResponse issuePresignedUrl(@NotNull PresignedUrlRequest request) {
		FileValidator.validateFileMetadata(request.fileName(), request.fileSize(), request.filePath());
		FileMetadata metadata = FileMetadataManager.createMetadataFromFileName(request.fileName(), request.filePath());

		PresignedUploadResult presignedResult = storageClient.generatePresignedUploadUrl(metadata, PRESIGNED_URL_EXPIRY);

		UuidFile pending = fileWriter.savePending(metadata, presignedResult.fileUrl());
		log.info("Presigned URL issued. UUID: {}, FilePath: {}", pending.getUuid(), request.filePath());
		return PresignedUrlResponse.of(pending, presignedResult);
	}

	/**
	 * 다중 파일 Presigned URL 발급
	 *
	 * @param request 파일 목록과 파일 경로 타입
	 * @return 각 파일의 presigned URL 목록 (요청 순서와 동일)
	 */
	@Transactional
	public MultiplePresignedUrlResponse issueMultiplePresignedUrls(@NotNull MultiplePresignedUrlRequest request) {
		List<String> fileNames = request.files().stream()
			.map(MultiplePresignedUrlRequest.FileEntry::fileName)
			.toList();
		List<Long> fileSizes = request.files().stream()
			.map(MultiplePresignedUrlRequest.FileEntry::fileSize)
			.toList();
		FileValidator.validateFileMetadataList(fileNames, fileSizes, request.filePath());

		List<PresignedUrlResponse> responses = request.files().stream()
			.map(entry -> {
				FileMetadata metadata = FileMetadataManager.createMetadataFromFileName(
					entry.fileName(), request.filePath());
				PresignedUploadResult presignedResult = storageClient.generatePresignedUploadUrl(
					metadata, PRESIGNED_URL_EXPIRY);
				UuidFile pending = fileWriter.savePending(metadata, presignedResult.fileUrl());
				return PresignedUrlResponse.of(pending, presignedResult);
			})
			.toList();

		log.info("Multiple presigned URLs issued. Count: {}, FilePath: {}", responses.size(), request.filePath());
		return MultiplePresignedUrlResponse.of(responses);
	}

	/**
	 * UUID 목록으로 PENDING 파일을 검증하고 CONFIRMED(isUsed=true)로 전환
	 * 도메인 생성 시 호출하여 presigned URL로 업로드된 파일들을 확정
	 *
	 * @param uuids            비즈니스 UUID 목록 (presigned URL 발급 응답의 uuid 값)
	 * @param expectedFilePath 해당 도메인에서 허용하는 파일 경로 타입
	 * @return 확인된 UuidFile 목록
	 */
	@Transactional
	public List<UuidFile> confirmFiles(@NotNull List<String> uuids, @NotNull FilePath expectedFilePath) {
		if (uuids.isEmpty()) {
			return List.of();
		}

		List<UuidFile> files = fileReader.findAllByUuids(uuids);
		if (files.size() != uuids.size()) {
			log.warn("Some UUIDs not found. Requested: {}, Found: {}", uuids.size(), files.size());
			throw FileErrorCode.FILE_NOT_FOUND.toBaseException();
		}

		for (UuidFile file : files) {
			if (file.getFilePath() != expectedFilePath) {
				log.warn("FilePath mismatch. Expected: {}, Actual: {}, UUID: {}",
					expectedFilePath, file.getFilePath(), file.getUuid());
				throw FileErrorCode.INVALID_FILE_PATH.toBaseException();
			}
			if (Boolean.TRUE.equals(file.getIsUsed())) {
				log.warn("File already used. UUID: {}", file.getUuid());
				throw FileErrorCode.FILE_ALREADY_USED.toBaseException();
			}
			if (!storageClient.exists(file.getFileKey())) {
				log.warn("File not found in storage. FileKey: {}", file.getFileKey());
				throw FileErrorCode.FILE_NOT_UPLOADED.toBaseException();
			}
		}

		fileWriter.markAsUsed(files);
		log.info("Files confirmed. Count: {}, FilePath: {}", files.size(), expectedFilePath);
		return files;
	}

	/**
	 * ID로 파일 조회
	 *
	 * @param id 파일 ID
	 * @return 파일 엔티티
	 */
	public UuidFile findUuidFileById(@NotBlank String id) {
		return fileReader.findById(id);
	}

	/**
	 * 파일 URL로 파일 조회
	 *
	 * @param fileUrl 파일 URL
	 * @return 파일 엔티티
	 */
	public UuidFile findUuidFileByFileUrl(
		@NotBlank String fileUrl) {
		return fileReader.findByFileUrl(fileUrl);
	}

	/**
	 * 파일 저장
	 *
	 * @param file     업로드할 파일
	 * @param filePath 파일 경로 타입
	 * @return 저장된 파일 엔티티
	 */
	@Transactional
	public UuidFile saveFile(
		@NotNull MultipartFile file,
		@NotNull FilePath filePath) {
		log.debug("Starting file save process. FilePath: {}", filePath);

		return fileWriter.uploadAndSave(file, filePath);
	}

	/**
	 * 파일 목록 저장
	 *
	 * @param fileList 업로드할 파일 목록
	 * @param filePath 파일 경로 타입
	 * @return 저장된 파일 엔티티 목록
	 */
	@Transactional
	public List<UuidFile> saveFileList(
		@NotNull List<MultipartFile> fileList,
		@NotNull FilePath filePath) {
		log.debug("Starting file list save process. Count: {}, FilePath: {}", fileList.size(), filePath);

		return fileWriter.uploadAndSaveList(fileList, filePath);
	}

	/**
	 * 파일 수정
	 * 기존 파일 삭제 후 새 파일 업로드
	 *
	 * @param fileId 기존 파일Id (null 가능)
	 * @param file          새 파일
	 * @param filePath      파일 경로 타입
	 * @return 저장된 파일 엔티티
	 */
	@Transactional
	public UuidFile updateFile(String fileId, @NotNull MultipartFile file, @NotNull FilePath filePath) {
		UuidFile priorUuidFile = findUuidFileById(fileId);
		log.debug("Starting file update process. PriorFileId: {}, FilePath: {}",
			priorUuidFile != null ? priorUuidFile.getId() : "null", filePath);

		FileValidator.validateFile(file, filePath);
		FileMetadata metadata = FileMetadataManager.createMetadata(file, filePath);

		return fileWriter.updateFile(priorUuidFile, file, metadata);
	}

	/**
	 * 파일 목록 수정
	 * 기존 파일들 삭제 후 새 파일들 업로드
	 *
	 * @param fileIdList 기존 파일 목록
	 * @param fileList          새 파일 목록
	 * @param filePath          파일 경로 타입
	 * @return 저장된 파일 엔티티 목록
	 */
	@Transactional
	public List<UuidFile> updateFileList(
		List<String> fileIdList,
		List<MultipartFile> fileList,
		FilePath filePath) {
		List<UuidFile> priorUuidFileList = fileReader.findByIds(fileIdList);
		log.debug("Starting file list update process. PriorCount: {}, NewCount: {}, FilePath: {}",
			priorUuidFileList != null ? priorUuidFileList.size() : 0,
			fileList.size(),
			filePath);

		if (priorUuidFileList != null && !priorUuidFileList.isEmpty()) {
			fileWriter.deleteList(priorUuidFileList);
		}

		return saveFileList(fileList, filePath);
	}

	/**
	 * 파일 삭제
	 *
	 * @param fileId 삭제할 파일 Id
	 */
	@Transactional
	public void deleteFile(String fileId) {
		UuidFile uuidFile = findUuidFileById(fileId);
		log.debug("Starting file delete process. FileId: {}", uuidFile.getId());
		fileWriter.delete(uuidFile);
	}

	/**
	 * 파일 목록 삭제
	 *
	 * @param fileIdList 삭제할 파일 엔티티 목록
	 */
	@Transactional
	public void deleteFileList(@NotNull List<String> fileIdList) {
		List<UuidFile> uuidFileList = fileReader.findByIds(fileIdList);
		log.debug("Starting file list delete process. Count: {}", uuidFileList.size());
		fileWriter.deleteList(uuidFileList);
	}
}
