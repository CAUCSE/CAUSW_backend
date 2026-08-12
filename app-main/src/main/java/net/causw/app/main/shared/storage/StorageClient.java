package net.causw.app.main.shared.storage;

import java.time.Duration;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.shared.storage.dto.FileMetadata;
import net.causw.app.main.shared.storage.dto.PresignedUploadResult;
import net.causw.app.main.shared.storage.dto.StorageResult;

/**
 * 파일 스토리지 업로드/삭제 인터페이스
 */
public interface StorageClient {

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

	/**
	 * 스토리지에서 파일 일괄 삭제
	 *
	 * @param fileKeys 삭제할 파일 키 목록
	 * @return 삭제에 성공한 파일 키 목록
	 */
	List<String> deleteAll(List<String> fileKeys);

	/**
	 * 클라이언트 직접 업로드용 Presigned PUT URL 생성
	 *
	 * @param metadata 파일 메타데이터
	 * @param expiry   URL 유효 기간
	 * @return presigned URL 및 최종 파일 URL
	 */
	PresignedUploadResult generatePresignedUploadUrl(FileMetadata metadata, Duration expiry);

	/**
	 * 스토리지에 파일이 실재하는지 확인 (HeadObject)
	 *
	 * @param fileKey 확인할 파일의 키
	 * @return 존재 여부
	 */
	boolean exists(String fileKey);
}
