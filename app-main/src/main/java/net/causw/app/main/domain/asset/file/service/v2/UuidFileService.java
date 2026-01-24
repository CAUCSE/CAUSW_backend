package net.causw.app.main.domain.asset.file.service.v2;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.v2.util.FileMetadataManager;
import net.causw.app.main.domain.asset.file.service.v2.util.FileValidator;
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;

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
	private final FileReader fileReader;
	private final FileWriter fileWriter;

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

		FileValidator.validateFile(file, filePath);

		FileMetadata metadata = FileMetadataManager.createMetadata(file, filePath);

		return fileWriter.uploadAndSave(file, metadata);
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

		FileValidator.validateFileList(fileList, filePath);

		List<FileMetadata> metadataList = fileList.stream()
			.map(file -> FileMetadataManager.createMetadata(file, filePath))
			.collect(Collectors.toList());

		return fileWriter.uploadAndSaveList(fileList, metadataList);
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
	 * @param uuidFileList 삭제할 파일 엔티티 목록
	 */
	@Transactional
	public void deleteFileList(@NotNull List<String> fileIdList) {
		List<UuidFile> uuidFileList = fileReader.findByIds(fileIdList);
		log.debug("Starting file list delete process. Count: {}", uuidFileList.size());
		fileWriter.deleteList(uuidFileList);
	}
}
