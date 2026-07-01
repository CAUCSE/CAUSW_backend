package net.causw.app.main.shared.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.shared.storage.dto.FileMetadata;
import net.causw.app.main.shared.storage.dto.StorageResult;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3 스토리지 업로더 구현체
 */
@Slf4j
@MeasureTime
@Component("s3StorageClient")
@Profile("!local")
@RequiredArgsConstructor
public class S3StorageClient implements StorageClient {

	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	@Override
	public StorageResult upload(MultipartFile file, FileMetadata metadata) {
		String fileKey = metadata.fileKey();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(fileKey)
			.contentType(file.getContentType())
			.contentDisposition(createContentDisposition(metadata))
			.acl(ObjectCannedACL.PUBLIC_READ)
			.build();

		try (InputStream inputStream = file.getInputStream()) {
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

			String fileUrl = s3Client.utilities()
				.getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileKey).build())
				.toString()
				.trim();

			log.debug("File uploaded successfully to S3. FileKey: {}, FileUrl: {}", fileKey, fileUrl);

			return StorageResult.of(
				fileKey,
				fileUrl,
				file.getSize(),
				Instant.now());

		} catch (IOException e) {
			log.error("Failed to upload file to S3. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_UPLOAD_FAIL,
				MessageUtil.FILE_UPLOAD_FAIL + e.getMessage());
		}
	}

	@Override
	public void delete(String fileKey) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build());
			log.debug("File deleted successfully from S3. FileKey: {}", fileKey);

		} catch (Exception e) {
			log.error("Failed to delete file from S3. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_DELETE_FAIL,
				MessageUtil.FILE_DELETE_FAIL + e.getMessage());
		}
	}

	private String createContentDisposition(FileMetadata metadata) {
		// Content-Disposition : attachment 헤더 추가
		// 브라우저가 파일을 열지 않고 다운로드하도록 설정
		return ContentDisposition.builder("attachment")
			.filename(metadata.originalFileName(), StandardCharsets.UTF_8)
			.build()
			.toString();
	}
}
