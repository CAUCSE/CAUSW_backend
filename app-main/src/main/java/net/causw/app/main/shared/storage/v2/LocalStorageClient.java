package net.causw.app.main.shared.storage.v2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
import net.causw.app.main.shared.storage.v2.dto.StorageResult;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 파일 시스템 스토리지 업로더 (테스트용)
 * 기본값: 홈디렉토리/causwfile 에 저장됨
 * application.yml의 storage.local.base-directory 설정으로 경로 변경 가능
 * 운영 환경에서는 사용하지 않음
 */
@Slf4j
@MeasureTime
@Component("localClient")
@Profile("local")
public class LocalStorageClient implements StorageClient {

	private final String baseDirectory;

	public LocalStorageClient(
		@Value("${storage.local.base-directory:#{systemProperties['user.home']}/causwfile}") String baseDirectory) {
		this.baseDirectory = baseDirectory.replace("/", File.separator);
		log.info("LocalStorageClient initialized with base directory: {}", this.baseDirectory);
	}

	@Override
	public StorageResult upload(MultipartFile file, FileMetadata metadata) {
		String fileKey = metadata.fileKey();
		Path filePath = Paths.get(baseDirectory, fileKey);

		try {
			Files.createDirectories(filePath.getParent());

			file.transferTo(filePath.toFile());

			String fileUrl = "file://" + filePath.toAbsolutePath();

			log.debug("File uploaded successfully to local storage. FileKey: {}, FilePath: {}", fileKey, filePath);

			return StorageResult.of(
				fileKey,
				fileUrl,
				file.getSize(),
				Instant.now());

		} catch (IOException e) {
			log.error("Failed to upload file to local storage. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_UPLOAD_FAIL,
				MessageUtil.FILE_UPLOAD_FAIL + e.getMessage());
		}
	}

	@Override
	public void delete(String fileKey) {
		Path filePath = Paths.get(baseDirectory, fileKey);

		try {
			File file = filePath.toFile();
			if (file.exists()) {
				Files.delete(filePath);
				log.debug("File deleted successfully from local storage. FileKey: {}", fileKey);
			} else {
				log.warn("File does not exist in local storage. FileKey: {}", fileKey);
			}

		} catch (IOException e) {
			log.error("Failed to delete file from local storage. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_DELETE_FAIL,
				MessageUtil.FILE_DELETE_FAIL + e.getMessage());
		}
	}
}
