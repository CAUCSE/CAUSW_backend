package net.causw.app.main.shared.storage.v2;

import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
import net.causw.app.main.shared.storage.v2.dto.StorageResult;

/**
 * 파일 스토리지 업로드/삭제 인터페이스
 */
public interface StorageUploader {

	/**
	 * 파일을 스토리지에 업로드
	 *
	 * @param file     업로드할 파일
	 * @param metadata 파일 메타데이터
	 * @return 업로드 결과
	 */
	StorageResult upload(MultipartFile file, FileMetadata metadata);

	/**
	 * 스토리지에서 파일 삭제
	 *
	 * @param fileKey 삭제할 파일의 키
	 */
	void delete(String fileKey);
}
