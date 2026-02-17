package net.causw.app.main.domain.asset.file.service.v2.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.repository.UuidFileRepository;
import net.causw.app.main.domain.asset.file.service.v2.util.FileMetadataManager;
import net.causw.app.main.shared.storage.v2.StorageClient;
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
import net.causw.app.main.shared.storage.v2.dto.StorageResult;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 및 삭제 전담 클래스
 * 스토리지 업로드와 DB 저장을 트랜잭션으로 관리
 */
@Slf4j
@MeasureTime
@Component
@RequiredArgsConstructor
public class FileWriter {

	private final StorageClient storageClient;
	private final UuidFileRepository uuidFileRepository;

	/**
	 * 파일 업로드 및 저장
	 * FilePath만 지정하면 메타데이터를 자동 생성하여 업로드
	 *
	 * @param file     업로드할 파일
	 * @param filePath 파일 저장 경로
	 * @return 저장된 파일 엔티티
	 */
	@Transactional
	public UuidFile uploadAndSave(@NotNull final MultipartFile file, @NotNull FilePath filePath) {
		FileMetadata metadata = FileMetadataManager.createMetadata(file, filePath);
		return uploadAndSave(file, metadata);
	}

	/**
	 * 파일 목록 업로드 및 저장
	 * FilePath만 지정하면 메타데이터를 자동 생성하여 업로드
	 *
	 * @param fileList 업로드할 파일 목록
	 * @param filePath 파일 저장 경로
	 * @return 저장된 파일 엔티티 목록
	 */
	@Transactional
	public List<UuidFile> uploadAndSaveList(@NotNull List<MultipartFile> fileList, @NotNull FilePath filePath) {
		List<FileMetadata> metadataList = fileList.stream()
			.map(it -> FileMetadataManager.createMetadata(it, filePath))
			.toList();
		return uploadAndSaveList(fileList, metadataList);
	}

	/**
	 * 파일 업로드 및 저장
	 * 스토리지 업로드 실패 시 예외 발생, DB 저장 실패 시 업로드된 파일 삭제
	 *
	 * @param file     업로드할 파일
	 * @param metadata 파일 메타데이터
	 * @return 저장된 파일 엔티티
	 */
	@Transactional
	public UuidFile uploadAndSave(@NotNull MultipartFile file, @NotNull FileMetadata metadata) {
		StorageResult storageResult = null;

		try {
			storageResult = uploadToStorage(file, metadata);

			UuidFile savedFile = saveToDatabase(storageResult, metadata);

			log.info("File uploaded and saved successfully. FileKey: {}, FileId: {}",
				storageResult.fileKey(), savedFile.getId());

			return savedFile;

		} catch (Exception e) {
			if (storageResult != null) {
				log.warn("DB save failed. Rolling back storage upload. FileKey: {}", storageResult.fileKey());
				try {
					storageClient.delete(storageResult.fileKey());
				} catch (Exception deleteException) {
					log.error("Failed to delete file during rollback. FileKey: {}",
						storageResult.fileKey(), deleteException);
				}
			}
			throw e;
		}
	}

	/**
	 * 파일 목록 업로드 및 저장
	 *
	 * @param fileList     업로드할 파일 목록
	 * @param metadataList 파일 메타데이터 목록
	 * @return 저장된 파일 엔티티 목록
	 */
	@Transactional
	public List<UuidFile> uploadAndSaveList(
		@NotNull List<MultipartFile> fileList,
		@NotNull List<FileMetadata> metadataList) {
		if (fileList.size() != metadataList.size()) {
			throw new IllegalArgumentException("File list size and metadata list size must be equal");
		}

		List<UuidFile> savedFiles = new ArrayList<>();
		List<String> uploadedFileKeys = new ArrayList<>();

		try {
			for (int i = 0; i < fileList.size(); i++) {
				MultipartFile file = fileList.get(i);
				FileMetadata metadata = metadataList.get(i);

				StorageResult storageResult = uploadToStorage(file, metadata);
				uploadedFileKeys.add(storageResult.fileKey());

				UuidFile savedFile = saveToDatabase(storageResult, metadata);
				savedFiles.add(savedFile);
			}

			log.info("Files uploaded and saved successfully. Count: {}", savedFiles.size());
			return savedFiles;

		} catch (Exception e) {
			log.warn("Batch upload failed. Rolling back {} uploaded files", uploadedFileKeys.size());
			rollbackUploadedFiles(uploadedFileKeys);
			throw e;
		}
	}

	/**
	 * 파일 업데이트 (기존 파일 삭제 후 새 파일 업로드)
	 *
	 * @param priorFile 기존 파일 (null 가능)
	 * @param newFile   새 파일
	 * @param metadata  새 파일 메타데이터
	 * @return 저장된 파일 엔티티
	 */
	@Transactional
	public UuidFile updateFile(UuidFile priorFile, @NotNull MultipartFile newFile, @NotNull FileMetadata metadata) {
		if (priorFile != null) {
			delete(priorFile);
		}

		return uploadAndSave(newFile, metadata);
	}

	/**
	 * 파일 삭제
	 * 스토리지 삭제 후 DB 삭제 순서로 진행
	 *
	 * @param file 삭제할 파일 엔티티
	 * @throws InternalServerException 파일이 null이거나 삭제 실패 시
	 */
	@Transactional
	public void delete(@NotNull UuidFile file) {
		if (file == null) {
			log.error("Attempted to delete null file");
			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_NOT_FOUND);
		}

		String fileKey = file.getFileKey();
		String fileId = file.getId();

		try {
			// 1. 스토리지에서 삭제
			storageClient.delete(fileKey);
			log.debug("File deleted from storage. FileKey: {}", fileKey);

			// 2. DB에서 삭제
			uuidFileRepository.delete(file);
			log.info("File deleted successfully. FileId: {}, FileKey: {}", fileId, fileKey);

		} catch (Exception e) {
			log.error("Failed to delete file. FileId: {}, FileKey: {}", fileId, fileKey, e);
			throw e;
		}
	}

	/**
	 * 파일 목록 삭제
	 *
	 * @param fileList 삭제할 파일 엔티티 목록
	 */
	@Transactional
	public void deleteList(@NotNull List<UuidFile> fileList) {
		if (fileList == null || fileList.isEmpty()) {
			log.warn("Attempted to delete empty or null file list");
			return;
		}

		int successCount = 0;
		int failCount = 0;

		for (UuidFile file : fileList) {
			try {
				delete(file);
				successCount++;
			} catch (Exception e) {
				failCount++;
				log.error("Failed to delete file in batch. FileId: {}, FileKey: {}",
					file.getId(), file.getFileKey(), e);
				// 개별 파일 삭제 실패 시에도 계속 진행
			}
		}

		log.info("Batch file deletion completed. Success: {}, Failed: {}, Total: {}",
			successCount, failCount, fileList.size());

		if (failCount > 0) {
			log.warn("{} files failed to delete out of {}", failCount, fileList.size());
		}
	}

	/**
	 * Soft Delete (논리적 삭제)
	 * 실제 파일은 삭제하지 않고 DB에서만 제거 또는 상태 변경
	 *
	 * @param file 삭제할 파일 엔티티
	 */
	//	@Transactional
	//	public void deleteSoft(@NotNull UuidFile file) {
	//		if (file == null) {
	//			log.error("Attempted to soft delete null file");
	//			throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_NOT_FOUND);
	//		}
	//
	//		try {
	//			uuidFileRepository.delete(file);
	//			log.info("File soft deleted. FileId: {}, FileKey: {}", file.getId(), file.getFileKey());
	//
	//		} catch (Exception e) {
	//			log.error("Failed to soft delete file. FileId: {}, FileKey: {}",
	//				file.getId(), file.getFileKey(), e);
	//			throw e;
	//		}
	//	}

	private StorageResult uploadToStorage(MultipartFile file, FileMetadata metadata) {
		log.debug("Uploading file to storage. FileKey: {}", metadata.fileKey());
		return storageClient.upload(file, metadata);
	}

	private UuidFile saveToDatabase(StorageResult result, FileMetadata metadata) {
		UuidFile uuidFile = UuidFile.of(
			metadata.uuid(),
			result.fileKey(),
			result.fileUrl(),
			metadata.rawFileName(),
			metadata.extension(),
			metadata.filePath());

		UuidFile saved = uuidFileRepository.save(uuidFile);
		log.debug("File entity saved to database. FileId: {}, FileKey: {}", saved.getId(), result.fileKey());

		return saved;
	}

	private void rollbackUploadedFiles(List<String> fileKeys) {
		for (String fileKey : fileKeys) {
			try {
				storageClient.delete(fileKey);
				log.debug("Rollback: Deleted file from storage. FileKey: {}", fileKey);
			} catch (Exception e) {
				log.error("Failed to delete file during rollback. FileKey: {}", fileKey, e);
			}
		}
	}
}
